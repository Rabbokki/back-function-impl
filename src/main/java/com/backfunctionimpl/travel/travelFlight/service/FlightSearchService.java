//package com.backfunctionimpl.travel.travelFlight.service;
//
//
//import com.backfunctionimpl.global.error.CustomException;
//import com.backfunctionimpl.global.error.ErrorCode;
//import com.backfunctionimpl.travel.travelFlight.dto.FlightInfo;
//import com.backfunctionimpl.travel.travelFlight.dto.FlightSearchReqDto;
//import com.backfunctionimpl.travel.travelFlight.dto.FlightSearchResDto;
//import com.backfunctionimpl.travel.travelFlight.entity.TravelFlight;
//import com.backfunctionimpl.travel.travelFlight.repository.TravelFlightRepository;
//import com.backfunctionimpl.travel.travelPlan.entity.TravelPlan;
//import com.backfunctionimpl.travel.travelPlan.repository.TravelPlanRepository;
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import jakarta.annotation.PostConstruct;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.cache.annotation.CacheEvict;
//import org.springframework.cache.annotation.Cacheable;
//import org.springframework.http.*;
//import org.springframework.stereotype.Service;
//import org.springframework.util.LinkedMultiValueMap;
//import org.springframework.util.MultiValueMap;
//import org.springframework.web.client.RestTemplate;
//import org.springframework.web.reactive.function.client.WebClient;
//import org.springframework.web.reactive.function.client.WebClientResponseException;
//import reactor.core.publisher.Mono;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.time.format.DateTimeParseException;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class FlightSearchService {
//
//    @Value("${amadeus.client-id}")
//    private String clientId;
//
//    @Value("${amadeus.client-secret}")
//    private String clientSecret;
//
//    private final ObjectMapper objectMapper = new ObjectMapper();
//    private final TravelFlightRepository travelFlightRepository;
//    private final TravelPlanRepository travelPlanRepository;
//    private String accessToken;
//    private long tokenExpiryTime;
//
//    private synchronized void fetchAccessToken() {
//        log.info("Fetching new Amadeus access token with clientId: {}", clientId);
//        WebClient client = WebClient.builder()
//                .baseUrl("https://test.api.amadeus.com")
//                .build();
//
//        try {
//            JsonNode response = client.post()
//                    .uri("/v1/security/oauth2/token")
//                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
//                    .bodyValue("grant_type=client_credentials&client_id=" + clientId + "&client_secret=" + clientSecret)
//                    .retrieve()
//                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), clientResponse -> {
//                        log.error("Token fetch error: status={}", clientResponse.statusCode());
//                        return clientResponse.bodyToMono(String.class)
//                                .map(body -> {
//                                    log.error("Token fetch error response: {}", body);
//                                    return new CustomException(ErrorCode.AMADEUS_API_ERROR);
//                                });
//                    })
//                    .bodyToMono(JsonNode.class)
//                    .block();
//
//            if (response != null && response.has("access_token")) {
//                accessToken = response.get("access_token").asText();
//                tokenExpiryTime = System.currentTimeMillis() + (response.get("expires_in").asLong() * 1000 - 60000);
//                log.info("Amadeus access token fetched successfully, expires in {} seconds", response.get("expires_in").asLong());
//            } else {
//                log.error("Invalid response from Amadeus token endpoint: {}", response);
//                throw new CustomException(ErrorCode.AMADEUS_API_ERROR);
//            }
//        } catch (Exception e) {
//            log.error("Failed to fetch Amadeus access token: {}", e.getMessage(), e);
//            throw new CustomException(ErrorCode.AMADEUS_API_ERROR);
//        }
//    }
//
//    private String getAccessToken() {
//        if (accessToken == null || System.currentTimeMillis() >= tokenExpiryTime) {
//            fetchAccessToken();
//        }
//        return accessToken;
//    }
//
//    @Cacheable(value = "flights", key = "#reqDto.origin + '-' + #reqDto.destination + '-' + #reqDto.departureDate", unless = "#reqDto.realTime")
//    public FlightSearchResDto searchFlights(FlightSearchReqDto reqDto) {
//        log.info("Searching flights for request: {}", reqDto);
//        validateSearchRequest(reqDto);
//
//        String originCode = mapCityToAirportCode(reqDto.getOrigin());
//        String destinationCode = mapCityToAirportCode(reqDto.getDestination());
//        log.debug("API parameters: originLocationCode={}, destinationLocationCode={}, departureDate={}",
//                originCode, destinationCode, reqDto.getDepartureDate());
//
//        WebClient client = WebClient.builder()
//                .baseUrl("https://test.api.amadeus.com")
//                .defaultHeader("Authorization", "Bearer " + getAccessToken())
//                .build();
//
//        try {
//            JsonNode response = client.get()
//                    .uri(uriBuilder -> uriBuilder
//                            .path("/v2/shopping/flight-offers")
//                            .queryParam("originLocationCode", originCode)
//                            .queryParam("destinationLocationCode", destinationCode)
//                            .queryParam("departureDate", reqDto.getDepartureDate())
//                            .queryParam("adults", 1)
//                            .queryParam("nonStop", true)
//                            .build())
//                    .retrieve()
//                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), clientResponse -> {
//                        log.error("Amadeus API error: status={}", clientResponse.statusCode());
//                        if (clientResponse.statusCode().value() == 401) {
//                            accessToken = null;
//                            fetchAccessToken();
//                            return Mono.error(new CustomException(ErrorCode.AMADEUS_API_ERROR));
//                        }
//                        return clientResponse.bodyToMono(String.class)
//                                .map(body -> {
//                                    log.error("Flight search error response: {}", body);
//                                    return new CustomException(ErrorCode.AMADEUS_API_ERROR);
//                                });
//                    })
//                    .bodyToMono(JsonNode.class)
//                    .block();
//
//            log.debug("Amadeus API response: {}", response != null ? response.toString() : "null");
//
//            List<FlightInfo> results = new ArrayList<>();
//            if (response != null && response.has("data") && !response.get("data").isNull()) {
//                JsonNode data = response.get("data");
//                log.debug("Data node: {}", data.toString());
//                for (JsonNode flight : data) {
//                    log.debug("Processing flight: {}", flight.toString());
//                    FlightInfo info = new FlightInfo();
//
//                    // ID
//                    info.setId(flight.has("id") && !flight.get("id").isNull() ? flight.get("id").asText() : null);
//
//                    // Price and Currency
//                    if (flight.has("price") && !flight.get("price").isNull()) {
//                        JsonNode priceNode = flight.get("price");
//                        info.setPrice(priceNode.has("total") && !priceNode.get("total").isNull() ? priceNode.get("total").asText() : null);
//                        info.setCurrency(priceNode.has("currency") && !priceNode.get("currency").isNull() ? priceNode.get("currency").asText() : "KRW");
//                    } else {
//                        log.warn("Price node missing or null in flight: {}", flight.toString());
//                    }
//
//                    // Number of Bookable Seats
//                    info.setNumberOfBookableSeats(flight.has("numberOfBookableSeats") && !flight.get("numberOfBookableSeats").isNull()
//                            ? flight.get("numberOfBookableSeats").asInt() : 0);
//
//                    // Itineraries and Segments
//                    if (flight.has("itineraries") && !flight.get("itineraries").isNull() && flight.get("itineraries").isArray() && flight.get("itineraries").size() > 0) {
//                        JsonNode itinerary = flight.get("itineraries").get(0);
//                        info.setDuration(itinerary.has("duration") && !itinerary.get("duration").isNull() ? itinerary.get("duration").asText() : null);
//
//                        if (itinerary.has("segments") && !itinerary.get("segments").isNull() && itinerary.get("segments").isArray() && itinerary.get("segments").size() > 0) {
//                            JsonNode segment = itinerary.get("segments").get(0);
//
//                            info.setCarrierCode(segment.has("carrierCode") && !segment.get("carrierCode").isNull() ? segment.get("carrierCode").asText() : "Unknown");
//                            info.setCarrier(mapCarrierCodeToName(info.getCarrierCode()));
//                            info.setFlightNumber(segment.has("number") && !segment.get("number").isNull() ? segment.get("number").asText() : "Unknown");
//
//                            // Departure
//                            if (segment.has("departure") && !segment.get("departure").isNull()) {
//                                JsonNode departure = segment.get("departure");
//                                info.setDepartureAirport(departure.has("iataCode") && !departure.get("iataCode").isNull() ? departure.get("iataCode").asText() : originCode);
//                                info.setDepartureTime(departure.has("at") && !departure.get("at").isNull() ? departure.get("at").asText() : null);
//                            } else {
//                                log.warn("Departure node missing or null in segment: {}", segment.toString());
//                                info.setDepartureAirport(originCode);
//                                info.setDepartureTime(null);
//                            }
//
//                            // Arrival
//                            if (segment.has("arrival") && !segment.get("arrival").isNull()) {
//                                JsonNode arrival = segment.get("arrival");
//                                info.setArrivalAirport(arrival.has("iataCode") && !arrival.get("iataCode").isNull() ? arrival.get("iataCode").asText() : destinationCode);
//                                info.setArrivalTime(arrival.has("at") && !arrival.get("at").isNull() ? arrival.get("at").asText() : null);
//                            } else {
//                                log.warn("Arrival node missing or null in segment: {}", segment.toString());
//                                info.setArrivalAirport(destinationCode);
//                                info.setArrivalTime(null);
//                            }
//
//                            // Aircraft
//                            if (segment.has("aircraft") && !segment.get("aircraft").isNull() && segment.get("aircraft").has("code") && !segment.get("aircraft").get("code").isNull()) {
//                                info.setAircraft(mapAircraftCode(segment.get("aircraft").get("code").asText()));
//                            } else {
//                                info.setAircraft("Unknown");
//                            }
//                        } else {
//                            log.warn("Segments node missing, null, or empty in itinerary: {}", itinerary.toString());
//                        }
//                    } else {
//                        log.warn("Itineraries node missing, null, or empty in flight: {}", flight.toString());
//                    }
//
//                    // Traveler Pricings and Cabin Baggage
//                    if (flight.has("travelerPricings") && !flight.get("travelerPricings").isNull() && flight.get("travelerPricings").isArray() && flight.get("travelerPricings").size() > 0) {
//                        JsonNode travelerPricing = flight.get("travelerPricings").get(0);
//                        if (travelerPricing.has("includedCabinBags") && !travelerPricing.get("includedCabinBags").isNull()) {
//                            JsonNode baggage = travelerPricing.get("includedCabinBags");
//                            String baggageInfo = baggage.has("quantity") && !baggage.get("quantity").isNull() ? "Quantity: " + baggage.get("quantity").asInt() : "";
//                            if (baggage.has("weight") && !baggage.get("weight").isNull()) {
//                                baggageInfo += (baggageInfo.isEmpty() ? "" : ", ") + "Weight: " + baggage.get("weight").asInt() + "kg";
//                            }
//                            info.setCabinBaggage(baggageInfo.isEmpty() ? "Unknown" : baggageInfo);
//                        } else {
//                            info.setCabinBaggage("Unknown");
//                        }
//                    } else {
//                        log.warn("TravelerPricings node missing, null, or empty in flight: {}", flight.toString());
//                        info.setCabinBaggage("Unknown");
//                    }
//
//                    // 필수 필드 확인
//                    if (info.getPrice() != null && info.getDepartureTime() != null && info.getArrivalTime() != null) {
//                        results.add(info);
//                        log.debug("Flight info added: {}", info);
//                    } else {
//                        log.warn("Skipping invalid flight data due to missing required fields: {}", flight.toString());
//                    }
//                }
//            } else {
//                log.warn("No valid data in Amadeus response for request: {}", reqDto);
//            }
//
//            FlightSearchResDto resDto = new FlightSearchResDto();
//            resDto.setSuccess(true);
//            resDto.setData(new FlightSearchResDto.FlightSearchData(results));
//            log.info("Returning {} flights for request: {}", results.size(), reqDto);
//            return resDto;
//        } catch (NullPointerException e) {
//            log.error("Failed to parse Amadeus response due to null field: {}", e.getMessage(), e);
//            throw new CustomException(ErrorCode.AMADEUS_API_ERROR);
//        } catch (Exception e) {
//            log.error("Unexpected error during Amadeus API call: {}", e.getMessage(), e);
//            throw new CustomException(ErrorCode.AMADEUS_API_ERROR);
//        }
//    }
//
//    public Long saveFlight(FlightSearchReqDto reqDto, Long travelPlanId) {
//        log.info("Saving flight for request: {}, travelPlanId: {}", reqDto, travelPlanId);
//        FlightSearchResDto result = searchFlights(reqDto);
//        if (result.getData().getFlights().isEmpty()) {
//            log.error("No flights found for request: {}", reqDto);
//            throw new CustomException(ErrorCode.INVALID_FLIGHT_SEARCH);
//        }
//
//        TravelPlan travelPlan = travelPlanRepository.findById(travelPlanId)
//                .orElseThrow(() -> {
//                    log.error("Travel plan not found: {}", travelPlanId);
//                    return new CustomException(ErrorCode.INVALID_FLIGHT_SEARCH);
//                });
//
//        FlightInfo flightInfo = result.getData().getFlights().get(0);
//        TravelFlight travelFlight = new TravelFlight();
//        travelFlight.setTravelPlan(travelPlan);
//        travelFlight.setAirlines(flightInfo.getCarrier());
//        travelFlight.setDepartureAirport(flightInfo.getDepartureAirport());
//        travelFlight.setArrivalAirport(flightInfo.getArrivalAirport());
//        travelFlight.setDepartureTime(LocalDateTime.parse(flightInfo.getDepartureTime()));
//        travelFlight.setArrivalTime(LocalDateTime.parse(flightInfo.getArrivalTime()));
//
//        travelFlight = travelFlightRepository.save(travelFlight);
//        log.info("Flight saved successfully with ID: {}", travelFlight.getId());
//        return travelFlight.getId();
//    }
//
//    @CacheEvict(value = "flights", key = "#reqDto.origin + '-' + #reqDto.destination + '-' + #reqDto.departureDate")
//    public void clearFlightCache(FlightSearchReqDto reqDto) {
//        log.info("Clearing flight cache for request: {}", reqDto);
//    }
//
//    public String mapCityToAirportCode(String city) {
//        Map<String, String> cityToAirport = new HashMap<>();
//        cityToAirport.put("osaka", "KIX");
//        cityToAirport.put("tokyo", "NRT");
//        cityToAirport.put("fukuoka", "FUK");
//        cityToAirport.put("paris", "CDG");
//        cityToAirport.put("rome", "FCO");
//        cityToAirport.put("venice", "VCE");
//        cityToAirport.put("bangkok", "BKK");
//        cityToAirport.put("singapore", "SIN");
//        cityToAirport.put("seoul", "ICN");
//        return cityToAirport.getOrDefault(city.toLowerCase(), "ICN");
//    }
//
//    private String mapCarrierCodeToName(String carrierCode) {
//        Map<String, String> carrierMap = new HashMap<>();
//        carrierMap.put("KE", "대한항공");
//        carrierMap.put("OZ", "아시아나항공");
//        carrierMap.put("7C", "제주항공");
//        carrierMap.put("LJ", "진에어");
//        carrierMap.put("RS", "에어서울");
//        carrierMap.put("TW", "티웨이항공");
//        carrierMap.put("BX", "에어부산");
//        carrierMap.put("ZE", "이스타항공");
//        return carrierMap.getOrDefault(carrierCode, carrierCode);
//    }
//
//    private String mapAircraftCode(String aircraftCode) {
//        Map<String, String> aircraftMap = new HashMap<>();
//        aircraftMap.put("789", "Boeing 787-9");
//        aircraftMap.put("380", "Airbus A380");
//        aircraftMap.put("737", "Boeing 737");
//        aircraftMap.put("320", "Airbus A320");
//        return aircraftMap.getOrDefault(aircraftCode, aircraftCode);
//    }
//
//    private void validateSearchRequest(FlightSearchReqDto reqDto) {
//        log.debug("Validating search request: {}", reqDto);
//        if (reqDto.getOrigin() == null || reqDto.getOrigin().isEmpty() ||
//                reqDto.getDestination() == null || reqDto.getDestination().isEmpty() ||
//                reqDto.getDepartureDate() == null || reqDto.getDepartureDate().isEmpty()) {
//            log.error("Invalid search request: missing required fields");
//            throw new CustomException(ErrorCode.INVALID_FLIGHT_SEARCH);
//        }
//        try {
//            LocalDate departureDate = LocalDate.parse(reqDto.getDepartureDate());
//            LocalDate maxDate = LocalDate.now().plusDays(330);
//            if (departureDate.isAfter(maxDate)) {
//                log.error("Departure date {} is after max allowed date {}", departureDate, maxDate);
//                throw new CustomException(ErrorCode.INVALID_FLIGHT_SEARCH);
//            }
//        } catch (DateTimeParseException e) {
//            log.error("Invalid departure date format: {}", reqDto.getDepartureDate());
//            throw new CustomException(ErrorCode.INVALID_FLIGHT_SEARCH);
//        }
//    }
//}