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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class FlightSearchServiceTwo {
    private final AmadeusClient amadeusClient;
    private final TravelFlightRepository travelFlightRepository;
    private final TravelPlanRepository travelPlanRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Pattern IATA_CODE_PATTERN = Pattern.compile("^[A-Z]{3}$");
    private static final Map<String, String> CITY_TO_IATA = new HashMap<>();

    static {
        CITY_TO_IATA.put("서울", "ICN");
        CITY_TO_IATA.put("도쿄", "NRT");
        CITY_TO_IATA.put("오사카", "KIX");
        CITY_TO_IATA.put("LA", "LAX");
        CITY_TO_IATA.put("파리", "CDG");
        CITY_TO_IATA.put("뉴욕", "JFK");
    }

    private static final Map<String, JsonNode> HARDCODED_FLIGHTS = new HashMap<>();

    static {
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode flights = mapper.createArrayNode();

        // ICN -> CDG
        ObjectNode flight1 = mapper.createObjectNode();
        flight1.put("id", "1");
        ObjectNode price1 = mapper.createObjectNode();
        price1.put("total", "1200.00");
        price1.put("currency", "KRW");
        flight1.set("price", price1);
        flight1.put("numberOfBookableSeats", 10);
        ArrayNode itineraries1 = mapper.createArrayNode();
        ObjectNode itinerary1 = mapper.createObjectNode();
        itinerary1.put("duration", "PT12H");
        ArrayNode segments1 = mapper.createArrayNode();
        ObjectNode segment1 = mapper.createObjectNode();
        segment1.put("carrierCode", "KE");
        segment1.put("number", "901");
        ObjectNode departure1 = mapper.createObjectNode();
        departure1.put("iataCode", "ICN");
        departure1.put("at", "2025-06-01T10:00:00");
        segment1.set("departure", departure1);
        ObjectNode arrival1 = mapper.createObjectNode();
        arrival1.put("iataCode", "CDG");
        arrival1.put("at", "2025-06-01T22:00:00");
        segment1.set("arrival", arrival1);
        ObjectNode aircraft1 = mapper.createObjectNode();
        aircraft1.put("code", "789");
        segment1.set("aircraft", aircraft1);
        segments1.add(segment1);
        itinerary1.set("segments", segments1);
        itineraries1.add(itinerary1);
        flight1.set("itineraries", itineraries1);
        ArrayNode travelerPricings1 = mapper.createArrayNode();
        ObjectNode travelerPricing1 = mapper.createObjectNode();
        ArrayNode fareDetails1 = mapper.createArrayNode();
        ObjectNode fareDetail1 = mapper.createObjectNode();
        ObjectNode baggage1 = mapper.createObjectNode();
        baggage1.put("quantity", 1);
        baggage1.put("weight", 23);
        fareDetail1.set("includedCheckedBags", baggage1);
        fareDetails1.add(fareDetail1);
        travelerPricing1.set("fareDetailsBySegment", fareDetails1);
        travelerPricings1.add(travelerPricing1);
        flight1.set("travelerPricings", travelerPricings1);
        flights.add(flight1);

        // CDG -> JFK
        ObjectNode flight2 = mapper.createObjectNode();
        flight2.put("id", "2");
        ObjectNode price2 = mapper.createObjectNode();
        price2.put("total", "800.00");
        price2.put("currency", "EUR");
        flight2.set("price", price2);
        flight2.put("numberOfBookableSeats", 15);
        ArrayNode itineraries2 = mapper.createArrayNode();
        ObjectNode itinerary2 = mapper.createObjectNode();
        itinerary2.put("duration", "PT8H");
        ArrayNode segments2 = mapper.createArrayNode();
        ObjectNode segment2 = mapper.createObjectNode();
        segment2.put("carrierCode", "AF");
        segment2.put("number", "006");
        ObjectNode departure2 = mapper.createObjectNode();
        departure2.put("iataCode", "CDG");
        departure2.put("at", "2025-06-01T09:00:00");
        segment2.set("departure", departure2);
        ObjectNode arrival2 = mapper.createObjectNode();
        arrival2.put("iataCode", "JFK");
        arrival2.put("at", "2025-06-01T17:00:00");
        segment2.set("arrival", arrival2);
        ObjectNode aircraft2 = mapper.createObjectNode();
        aircraft2.put("code", "380");
        segment2.set("aircraft", aircraft2);
        segments2.add(segment2);
        itinerary2.set("segments", segments2);
        itineraries2.add(itinerary2);
        flight2.set("itineraries", itineraries2);
        ArrayNode travelerPricings2 = mapper.createArrayNode();
        ObjectNode travelerPricing2 = mapper.createObjectNode();
        ArrayNode fareDetails2 = mapper.createArrayNode();
        ObjectNode fareDetail2 = mapper.createObjectNode();
        ObjectNode baggage2 = mapper.createObjectNode();
        baggage2.put("quantity", 2);
        baggage2.put("weight", 23);
        fareDetail2.set("includedCheckedBags", baggage2);
        fareDetails2.add(fareDetail2);
        travelerPricing2.set("fareDetailsBySegment", fareDetails2);
        travelerPricings2.add(travelerPricing2);
        flight2.set("travelerPricings", travelerPricings2);
        flights.add(flight2);

        HARDCODED_FLIGHTS.put("ICN-CDG-2025-06-01", flights);
        HARDCODED_FLIGHTS.put("CDG-JFK-2025-06-01", flights);
    }

    @Cacheable(value = "flights", key = "#reqDto.origin + '-' + #reqDto.destination + '-' + #reqDto.departureDate", unless = "#reqDto.realTime")
    public FlightSearchResDto searchFlights(FlightSearchReqDto reqDto) {
        log.info("Searching flights for request: {}", reqDto);
        validateSearchRequest(reqDto);

        // IATA 코드 변환
        String origin = convertToIataCode(reqDto.getOrigin());
        String destination = convertToIataCode(reqDto.getDestination());
        reqDto.setOrigin(origin);
        reqDto.setDestination(destination);

        // 하드코딩된 비행 데이터 확인
        String flightKey = origin + "-" + destination + "-" + reqDto.getDepartureDate();
        if (HARDCODED_FLIGHTS.containsKey(flightKey)) {
            log.info("Returning hardcoded flights for key: {}", flightKey);
            JsonNode data = HARDCODED_FLIGHTS.get(flightKey);
            return processFlightData(data);
        }

        try {
            WebClient client = WebClient.builder()
                    .baseUrl("https://test.api.amadeus.com")
                    .defaultHeader("Authorization", "Bearer " + amadeusClient.getAccessToken())
                    .build();

            JsonNode response = client.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v2/shopping/flight-offers")
                            .queryParam("originLocationCode", origin)
                            .queryParam("destinationLocationCode", destination)
                            .queryParam("departureDate", reqDto.getDepartureDate())
                            .queryParam("adults", 1)
                            .queryParam("nonStop", reqDto.isRealTime())
                            .build())
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), clientResponse -> {
                        log.error("Amadeus API error: status={}", clientResponse.statusCode());
                        return clientResponse.bodyToMono(String.class)
                                .map(body -> {
                                    log.error("Flight search error response: {}", body);
                                    try {
                                        JsonNode errorJson = objectMapper.readTree(body);
                                        String detail = errorJson.path("errors").path(0).path("detail").asText("Unknown error");
                                        return new CustomException(ErrorCode.AMADEUS_API_ERROR);
                                    } catch (Exception e) {
                                        return new CustomException(ErrorCode.AMADEUS_API_ERROR);
                                    }
                                });
                    })
                    .bodyToMono(JsonNode.class)
                    .block();

            return processFlightData(response != null && response.has("data") ? response.get("data") : objectMapper.createArrayNode());
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

    private FlightSearchResDto processFlightData(JsonNode data) {
        List<FlightInfo> results = new ArrayList<>();
        if (data != null && !data.isNull()) {
            for (JsonNode flight : data) {
                FlightInfo info = new FlightInfo();
                info.setId(flight.has("id") ? flight.get("id").asText() : null);

                if (flight.has("price") && !flight.get("price").isNull()) {
                    JsonNode priceNode = flight.get("price");
                    info.setPrice(priceNode.has("total") ? priceNode.get("total").asText() : null);
                    info.setCurrency(priceNode.has("currency") ? priceNode.get("currency").asText() : "KRW");
                }

                info.setNumberOfBookableSeats(flight.has("numberOfBookableSeats") ? flight.get("numberOfBookableSeats").asInt() : 0);

                if (flight.has("itineraries") && !flight.get("itineraries").isNull() && flight.get("itineraries").isArray() && flight.get("itineraries").size() > 0) {
                    JsonNode itinerary = flight.get("itineraries").get(0);
                    info.setDuration(itinerary.has("duration") ? itinerary.get("duration").asText() : null);

                    if (itinerary.has("segments") && !itinerary.get("segments").isNull() && itinerary.get("segments").isArray() && itinerary.get("segments").size() > 0) {
                        JsonNode segment = itinerary.get("segments").get(0);
                        info.setCarrierCode(segment.has("carrierCode") ? segment.get("carrierCode").asText() : "Unknown");
                        info.setCarrier(mapCarrierCodeToName(info.getCarrierCode()));
                        info.setFlightNumber(segment.has("number") ? segment.get("number").asText() : "Unknown");

                        if (segment.has("departure") && !segment.get("departure").isNull()) {
                            JsonNode departure = segment.get("departure");
                            info.setDepartureAirport(departure.has("iataCode") ? departure.get("iataCode").asText() : null);
                            info.setDepartureTime(departure.has("at") ? departure.get("at").asText() : null);
                        }

                        if (segment.has("arrival") && !segment.get("arrival").isNull()) {
                            JsonNode arrival = segment.get("arrival");
                            info.setArrivalAirport(arrival.has("iataCode") ? arrival.get("iataCode").asText() : null);
                            info.setArrivalTime(arrival.has("at") ? arrival.get("at").asText() : null);
                        }

                        if (segment.has("aircraft") && !segment.get("aircraft").isNull() && segment.get("aircraft").has("code")) {
                            info.setAircraft(mapAircraftCode(segment.get("aircraft").get("code").asText()));
                        } else {
                            info.setAircraft("Unknown");
                        }
                    }
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
        log.info("Returning {} flights for request", results.size());
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

    private String mapCarrierCodeToName(String carrierCode) {
        Map<String, String> carrierMap = new HashMap<>();
        carrierMap.put("KE", "대한항공");
        carrierMap.put("OZ", "아시아나항공");
        carrierMap.put("7C", "제주항공");
        carrierMap.put("LJ", "진에어");
        carrierMap.put("RS", "에어서울");
        carrierMap.put("TW", "티웨이항공");
        carrierMap.put("BX", "에어부산");
        carrierMap.put("ZE", "이스타항공");
        return carrierMap.getOrDefault(carrierCode, carrierCode);
    }

    private String mapAircraftCode(String aircraftCode) {
        Map<String, String> aircraftMap = new HashMap<>();
        aircraftMap.put("789", "Boeing 787-9");
        aircraftMap.put("380", "Airbus A380");
        aircraftMap.put("737", "Boeing 737");
        aircraftMap.put("320", "Airbus A320");
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
        } catch (DateTimeParseException e) {
            log.error("Invalid departure date format: {}", reqDto.getDepartureDate());
            throw new CustomException(ErrorCode.INVALID_FLIGHT_SEARCH);
        }
    }

    private String convertToIataCode(String input) {
        if (IATA_CODE_PATTERN.matcher(input).matches()) {
            return input;
        }
        String iata = CITY_TO_IATA.get(input);
        if (iata == null) {
            log.error("Invalid input: {}. Must be a 3-letter IATA code or supported city name.", input);
            throw new CustomException(ErrorCode.INVALID_FLIGHT_SEARCH);
        }
        return iata;
    }
}