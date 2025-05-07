package com.backfunctionimpl.travel.travelFlight.service;

import com.amadeus.Params;
import com.amadeus.exceptions.ResponseException;
import com.amadeus.resources.FlightOfferSearch;
import com.backfunctionimpl.global.error.CustomException;
import com.backfunctionimpl.global.error.ErrorCode;
import com.backfunctionimpl.travel.config.AmadeusClient;
import com.backfunctionimpl.travel.travelFlight.dto.FlightInfo;
import com.backfunctionimpl.travel.travelFlight.dto.FlightSearchReqDto;
import com.backfunctionimpl.travel.travelFlight.dto.FlightSearchResDto;
import com.backfunctionimpl.travel.travelFlight.entity.TravelFlight;
import com.backfunctionimpl.travel.travelFlight.repository.TravelFlightRepository;
import com.backfunctionimpl.travel.travelPlan.entity.TravelPlan;
import com.backfunctionimpl.travel.travelPlan.repository.TravelPlanRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class FlightSearchServiceTwo {
    private final AmadeusClient amadeusClient;
    private final TravelFlightRepository travelFlightRepository;
    private final TravelPlanRepository travelPlanRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Cacheable(value = "flights", key = "#reqDto.origin + '-' + #reqDto.destination + '-' + #reqDto.departureDate", unless = "#reqDto.realTime")
    public FlightSearchResDto searchFlights(FlightSearchReqDto reqDto) {
        log.info("Searching flights for request: {}", reqDto);
        validateSearchRequest(reqDto);

        String origin = reqDto.getOrigin();
        String destination = reqDto.getDestination();

        try {
            WebClient client = WebClient.builder()
                    .baseUrl("https://test.api.amadeus.com")
                    .defaultHeader("Authorization", "Bearer " + amadeusClient.getAccessToken())
                    .build();

            JsonNode response = client.get()
                    .uri(uriBuilder -> {
                        uriBuilder
                                .path("/v2/shopping/flight-offers")
                                .queryParam("originLocationCode", origin)
                                .queryParam("destinationLocationCode", destination)
                                .queryParam("departureDate", reqDto.getDepartureDate())
                                .queryParam("adults", 1)
                                .queryParam("nonStop", true)
                                .queryParam("currencyCode", "EUR");
                        if (reqDto.getReturnDate() != null && !reqDto.getReturnDate().isEmpty()) {
                            uriBuilder.queryParam("returnDate", reqDto.getReturnDate());
                        }
                        return uriBuilder.build();
                    })
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), clientResponse -> {
                        log.error("Amadeus API error: status={}", clientResponse.statusCode());
                        return clientResponse.bodyToMono(String.class)
                                .map(body -> {
                                    log.error("Flight search error response: {}", body);
                                    try {
                                        JsonNode errorJson = objectMapper.readTree(body);
                                        String detail = errorJson.path("errors").path(0).path("detail").asText("Unknown error");
                                        log.error("Amadeus API error detail: {}", detail);
                                        return new CustomException(ErrorCode.AMADEUS_API_ERROR);
                                    } catch (Exception e) {
                                        return new CustomException(ErrorCode.AMADEUS_API_ERROR);
                                    }
                                });
                    })
                    .bodyToMono(JsonNode.class)
                    .block();

            log.info("Amadeus API response: {}", response);
            if (response == null || !response.has("data")) {
                log.warn("No flight data returned from Amadeus API");
                return new FlightSearchResDto(true, new ArrayList<>());
            }
            return processFlightData(response.get("data"), origin, destination, reqDto.getReturnDate() != null);
        } catch (RedisConnectionFailureException e) {
            log.error("Redis connection failed: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.REDIS_CONNECTION_ERROR);
        } catch (SerializationException e) {
            log.error("Redis serialization failed: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.REDIS_SERIALIZATION_ERROR);
        } catch (CustomException e) {
            log.error("Amadeus API error: {}", e.getErrorCode().getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during Amadeus API call: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private FlightSearchResDto processFlightData(JsonNode data, String origin, String requestedDestination, boolean isRoundTrip) {
        List<FlightInfo> results = new ArrayList<>();
        Map<String, String> carrierMap = getCarrierMap();
        Set<String> uniqueFlights = new HashSet<>(); // 중복 항공편 제거

        // 노선별 최소 소요 시간 (시간 단위)
        Map<String, Integer> minDurationMap = new HashMap<>();
        minDurationMap.put("CDG-JFK", 7); // 7시간
        minDurationMap.put("JFK-CDG", 6); // 6시간
        minDurationMap.put("CDG-NCE", 1); // 1시간
        minDurationMap.put("CDG-LHR", 1); // 1시간
        minDurationMap.put("CDG-FRA", 1); // 1시간
        minDurationMap.put("CDG-MAD", 2); // 2시간
        minDurationMap.put("CDG-YUL", 7); // 7시간
        minDurationMap.put("CDG-KEF", 3); // 3시간
        minDurationMap.put("CDG-LIS", 2); // 2시간
        minDurationMap.put("CDG-IST", 3); // 3시간
        minDurationMap.put("CDG-BEG", 2); // 2시간
        minDurationMap.put("CDG-CMN", 3); // 3시간

        if (data != null && !data.isNull()) {
            for (JsonNode flight : data) {
                // 여정 검증
                boolean validItineraries = true;
                String carrierCode = null;
                String flightKey = null;

                if (!flight.has("itineraries") || !flight.get("itineraries").isArray()) {
                    log.warn("Skipping flight with invalid itineraries: {}", flight.toString());
                    continue;
                }

                JsonNode itineraries = flight.get("itineraries");
                // 출발 여정 (CDG -> JFK)
                if (itineraries.size() > 0) {
                    JsonNode outbound = itineraries.get(0);
                    if (!outbound.has("segments") || !outbound.get("segments").isArray() || outbound.get("segments").size() == 0) {
                        log.warn("Skipping flight with invalid outbound segments: {}", flight.toString());
                        continue;
                    }
                    JsonNode segment = outbound.get("segments").get(0);
                    String departureAirport = segment.has("departure") && segment.get("departure").has("iataCode") ? segment.get("departure").get("iataCode").asText() : "";
                    String arrivalAirport = segment.has("arrival") && segment.get("arrival").has("iataCode") ? segment.get("arrival").get("iataCode").asText() : "";
                    carrierCode = segment.has("carrierCode") ? segment.get("carrierCode").asText() : "Unknown";

                    if (!departureAirport.equals(origin) || !arrivalAirport.equals(requestedDestination)) {
                        log.warn("Skipping flight with invalid outbound route: {} -> {}, expected: {} -> {}", departureAirport, arrivalAirport, origin, requestedDestination);
                        validItineraries = false;
                    }

                    // 항공사 코드 검증
                    if (!carrierMap.containsKey(carrierCode) && !carrierCode.equals("Unknown")) {
                        log.warn("Skipping flight with unknown carrier code: {}", carrierCode);
                        validItineraries = false;
                    }

                    // 소요 시간 검증
                    String routeKey = departureAirport + "-" + arrivalAirport;
                    if (outbound.has("duration")) {
                        try {
                            Duration duration = Duration.parse(outbound.get("duration").asText());
                            int minHours = minDurationMap.getOrDefault(routeKey, 1);
                            if (duration.isNegative() || duration.isZero() || duration.toHours() < minHours) {
                                log.warn("Skipping flight with invalid outbound duration: {} for route {}", duration, routeKey);
                                validItineraries = false;
                            }
                        } catch (Exception e) {
                            log.warn("Invalid outbound duration format: {}", outbound.get("duration").asText());
                            validItineraries = false;
                        }
                    }
                }

                // 귀국 여정 (JFK -> CDG)
                if (isRoundTrip && itineraries.size() > 1) {
                    JsonNode inbound = itineraries.get(1);
                    if (!inbound.has("segments") || !inbound.get("segments").isArray() || inbound.get("segments").size() == 0) {
                        log.warn("Skipping flight with invalid inbound segments: {}", flight.toString());
                        continue;
                    }
                    JsonNode segment = inbound.get("segments").get(0);
                    String departureAirport = segment.has("departure") && segment.get("departure").has("iataCode") ? segment.get("departure").get("iataCode").asText() : "";
                    String arrivalAirport = segment.has("arrival") && segment.get("arrival").has("iataCode") ? segment.get("arrival").get("iataCode").asText() : "";
                    String inboundCarrierCode = segment.has("carrierCode") ? segment.get("carrierCode").asText() : "Unknown";

                    if (!departureAirport.equals(requestedDestination) || !arrivalAirport.equals(origin)) {
                        log.warn("Skipping flight with invalid inbound route: {} -> {}, expected: {} -> {}", departureAirport, arrivalAirport, requestedDestination, origin);
                        validItineraries = false;
                    }

                    if (!carrierMap.containsKey(inboundCarrierCode) && !inboundCarrierCode.equals("Unknown")) {
                        log.warn("Skipping flight with unknown inbound carrier code: {}", inboundCarrierCode);
                        validItineraries = false;
                    }

                    String routeKey = departureAirport + "-" + arrivalAirport;
                    if (inbound.has("duration")) {
                        try {
                            Duration duration = Duration.parse(inbound.get("duration").asText());
                            int minHours = minDurationMap.getOrDefault(routeKey, 1);
                            if (duration.isNegative() || duration.isZero() || duration.toHours() < minHours) {
                                log.warn("Skipping flight with invalid inbound duration: {} for route {}", duration, routeKey);
                                validItineraries = false;
                            }
                        } catch (Exception e) {
                            log.warn("Invalid inbound duration format: {}", inbound.get("duration").asText());
                            validItineraries = false;
                        }
                    }
                }

                if (!validItineraries) {
                    continue;
                }

                // 중복 항공편 체크
                String departureTime = "";
                String arrivalTime = "";
                String price = "";
                JsonNode segment = itineraries.get(0).get("segments").get(0);
                departureTime = segment.has("departure") && segment.get("departure").has("at") ? segment.get("departure").get("at").asText() : "";
                arrivalTime = segment.has("arrival") && segment.get("arrival").has("at") ? segment.get("arrival").get("at").asText() : "";
                price = flight.has("price") && flight.get("price").has("total") ? flight.get("price").get("total").asText() : "";
                flightKey = carrierCode + "-" + departureTime + "-" + arrivalTime + "-" + price;

                if (!uniqueFlights.add(flightKey)) {
                    log.warn("Skipping duplicate flight: {}", flightKey);
                    continue;
                }

                // FlightInfo 생성
                FlightInfo info = new FlightInfo();
                info.setId(flight.has("id") ? flight.get("id").asText() : null);

                if (flight.has("price") && !flight.get("price").isNull()) {
                    JsonNode priceNode = flight.get("price");
                    info.setPrice(priceNode.has("total") ? priceNode.get("total").asText() : null);
                    info.setCurrency(priceNode.has("currency") ? priceNode.get("currency").asText() : "EUR");
                }

                info.setNumberOfBookableSeats(flight.has("numberOfBookableSeats") ? flight.get("numberOfBookableSeats").asInt() : 0);

                JsonNode outbound = itineraries.get(0);
                info.setDuration(outbound.has("duration") ? outbound.get("duration").asText() : null);

                JsonNode segmentOutbound = outbound.get("segments").get(0);
                info.setCarrierCode(carrierCode);
                info.setCarrier(mapCarrierCodeToName(carrierCode));
                info.setFlightNumber(segmentOutbound.has("number") ? segmentOutbound.get("number").asText() : "Unknown");

                if (segmentOutbound.has("departure") && !segmentOutbound.get("departure").isNull()) {
                    JsonNode departure = segmentOutbound.get("departure");
                    info.setDepartureAirport(departure.has("iataCode") ? departure.get("iataCode").asText() : null);
                    info.setDepartureTime(departure.has("at") ? departure.get("at").asText() : null);
                }

                if (segmentOutbound.has("arrival") && !segmentOutbound.get("arrival").isNull()) {
                    JsonNode arrival = segmentOutbound.get("arrival");
                    info.setArrivalAirport(arrival.has("iataCode") ? arrival.get("iataCode").asText() : null);
                    info.setArrivalTime(arrival.has("at") ? arrival.get("at").asText() : null);
                }

                if (segmentOutbound.has("aircraft") && !segmentOutbound.get("aircraft").isNull() && segmentOutbound.get("aircraft").has("code")) {
                    info.setAircraft(mapAircraftCode(segmentOutbound.get("aircraft").get("code").asText()));
                } else {
                    info.setAircraft("Unknown");
                }

                if (flight.has("travelerPricings") && !flight.get("travelerPricings").isNull() && flight.get("travelerPricings").isArray() && flight.get("travelerPricings").size() > 0) {
                    JsonNode travelerPricing = flight.get("travelerPricings").get(0);
                    if (travelerPricing.has("fareDetailsBySegment") && !travelerPricing.get("fareDetailsBySegment").isNull() && travelerPricing.get("fareDetailsBySegment").isArray()) {
                        JsonNode fareDetails = travelerPricing.get("fareDetailsBySegment").get(0);
                        String baggageInfo = "";
                        if (fareDetails.has("includedCheckedBags") && !fareDetails.get("includedCheckedBags").isNull()) {
                            JsonNode baggage = fareDetails.get("includedCheckedBags");
                            baggageInfo = baggage.has("quantity") ? "Quantity: " + baggage.get("quantity").asInt() : "";
                            if (baggage.has("weight") && !baggage.get("weight").isNull()) {
                                baggageInfo += (baggageInfo.isEmpty() ? "" : ", ") + "Weight: " + baggage.get("weight").asInt() + "kg";
                            }
                        }
                        info.setCabinBaggage(baggageInfo.isEmpty() ? "Unknown" : baggageInfo);
                    } else {
                        info.setCabinBaggage("Unknown");
                    }
                } else {
                    info.setCabinBaggage("Unknown");
                }

                if (info.getPrice() != null && info.getDepartureTime() != null && info.getArrivalTime() != null) {
                    results.add(info);
                } else {
                    log.warn("Skipping invalid flight data: {}", flight.toString());
                }
            }
        }

        FlightSearchResDto resDto = new FlightSearchResDto();
        resDto.setSuccess(true);
        resDto.setFlights(results);
        log.info("Returning {} flights for destination {}", results.size(), requestedDestination);
        return resDto;
    }

    public Long saveFlight(FlightSearchReqDto reqDto, Long travelPlanId) {
        log.info("Saving flight for request: {}, travelPlanId: {}", reqDto, travelPlanId);
        FlightSearchResDto result = searchFlights(reqDto);
        if (result.getFlights().isEmpty()) {
            log.error("No flights found for request: {}", reqDto);
            throw new CustomException(ErrorCode.INVALID_FLIGHT_SEARCH);
        }

        TravelPlan travelPlan = travelPlanRepository.findById(travelPlanId)
                .orElseThrow(() -> {
                    log.error("Travel plan not found: {}", travelPlanId);
                    return new CustomException(ErrorCode.INVALID_FLIGHT_SEARCH);
                });

        FlightInfo flightInfo = result.getFlights().get(0);
        TravelFlight travelFlight = new TravelFlight();
        travelFlight.setTravelPlan(travelPlan);
        travelFlight.setAirlines(flightInfo.getCarrier());
        travelFlight.setDepartureAirport(flightInfo.getDepartureAirport());
        travelFlight.setArrivalAirport(flightInfo.getArrivalAirport());
        travelFlight.setDepartureTime(LocalDateTime.parse(flightInfo.getDepartureTime()));
        travelFlight.setArrivalTime(LocalDateTime.parse(flightInfo.getArrivalTime()));

        travelFlight = travelFlightRepository.save(travelFlight);
        log.info("Flight saved successfully with ID: {}", travelFlight.getId());
        return travelFlight.getId();
    }

    @CacheEvict(value = "flights", key = "#reqDto.origin + '-' + #reqDto.destination + '-' + #reqDto.departureDate")
    public void clearFlightCache(FlightSearchReqDto reqDto) {
        log.info("Clearing flight cache for request: {}", reqDto);
    }

    private Map<String, String> getCarrierMap() {
        Map<String, String> carrierMap = new HashMap<>();
        carrierMap.put("KE", "대한항공");
        carrierMap.put("OZ", "아시아나항공");
        carrierMap.put("AF", "Air France");
        carrierMap.put("DL", "Delta Air Lines");
        carrierMap.put("LH", "Lufthansa");
        carrierMap.put("IB", "Iberia");
        carrierMap.put("UA", "United Airlines");
        carrierMap.put("BA", "British Airways");
        carrierMap.put("FI", "Icelandair");
        carrierMap.put("TK", "Turkish Airlines");
        carrierMap.put("JU", "Air Serbia");
        carrierMap.put("AT", "Royal Air Maroc");
        return carrierMap;
    }

    private String mapCarrierCodeToName(String carrierCode) {
        return getCarrierMap().getOrDefault(carrierCode, carrierCode);
    }

    private String mapAircraftCode(String aircraftCode) {
        Map<String, String> aircraftMap = new HashMap<>();
        aircraftMap.put("789", "Boeing 787-9");
        aircraftMap.put("380", "Airbus A380");
        aircraftMap.put("737", "Boeing 737");
        aircraftMap.put("320", "Airbus A320");
        aircraftMap.put("767", "Boeing 767");
        return aircraftMap.getOrDefault(aircraftCode, aircraftCode);
    }

    private void validateSearchRequest(FlightSearchReqDto reqDto) {
        log.debug("Validating search request: {}", reqDto);
        if (reqDto.getOrigin() == null || reqDto.getOrigin().isEmpty() ||
                reqDto.getDestination() == null || reqDto.getDestination().isEmpty() ||
                reqDto.getDepartureDate() == null || reqDto.getDepartureDate().isEmpty()) {
            log.error("Invalid search request: missing required fields");
            throw new CustomException(ErrorCode.INVALID_FLIGHT_SEARCH);
        }
        try {
            LocalDate departureDate = LocalDate.parse(reqDto.getDepartureDate());
            LocalDate maxDate = LocalDate.now().plusDays(330);
            if (departureDate.isAfter(maxDate)) {
                log.error("Departure date {} is after max allowed date {}", departureDate, maxDate);
                throw new CustomException(ErrorCode.INVALID_FLIGHT_SEARCH);
            }
            if (reqDto.getReturnDate() != null && !reqDto.getReturnDate().isEmpty()) {
                LocalDate returnDate = LocalDate.parse(reqDto.getReturnDate());
                if (returnDate.isBefore(departureDate)) {
                    log.error("Return date {} is before departure date {}", returnDate, departureDate);
                    throw new CustomException(ErrorCode.INVALID_FLIGHT_SEARCH);
                }
            }
        } catch (DateTimeParseException e) {
            log.error("Invalid date format: departureDate={}, returnDate={}", reqDto.getDepartureDate(), reqDto.getReturnDate());
            throw new CustomException(ErrorCode.INVALID_FLIGHT_SEARCH);
        }
    }
}