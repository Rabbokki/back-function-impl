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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Pattern;

@Service
@Slf4j
public class FlightSearchServiceTwo {
    private final AmadeusClient amadeusClient;
    private final TravelFlightRepository travelFlightRepository;
    private final TravelPlanRepository travelPlanRepository;
    private final ObjectMapper objectMapper;

    public FlightSearchServiceTwo(AmadeusClient amadeusClient, TravelFlightRepository travelFlightRepository, TravelPlanRepository travelPlanRepository) {
        this.amadeusClient = amadeusClient;
        this.travelFlightRepository = travelFlightRepository;
        this.travelPlanRepository = travelPlanRepository;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public FlightSearchResDto searchFlights(FlightSearchReqDto reqDto) {
        try {
            log.info("검색 요청: {}", objectMapper.writeValueAsString(reqDto));
        } catch (Exception e) {
            log.error("요청 직렬화 실패: {}", e.getMessage());
        }

        validateSearchRequest(reqDto);

        String origin = reqDto.getOrigin();
        String destination = reqDto.getDestination();
        boolean isRoundTrip = reqDto.isRealTime() &&
                reqDto.getReturnDate() != null &&
                !reqDto.getReturnDate().isEmpty();

        WebClient client = WebClient.builder()
                .baseUrl("https://test.api.amadeus.com")
                .defaultHeader("Authorization", "Bearer " + amadeusClient.getAccessToken())
                .build();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("currencyCode", "KRW");

        List<Map<String, Object>> originDestinations = new ArrayList<>();
        originDestinations.add(Map.of(
                "id", "1",
                "originLocationCode", origin,
                "destinationLocationCode", destination,
                "departureDateTimeRange", Map.of("date", reqDto.getDepartureDate())
        ));

        if (isRoundTrip) {
            originDestinations.add(Map.of(
                    "id", "2",
                    "originLocationCode", destination,
                    "destinationLocationCode", origin,
                    "departureDateTimeRange", Map.of("date", reqDto.getReturnDate())
            ));
            log.info("왕복 포함됨: 귀국 날짜 = {}", reqDto.getReturnDate());
        }

        requestBody.put("originDestinations", originDestinations);
        requestBody.put("travelers", List.of(Map.of("id", "1", "travelerType", "ADULT")));
        requestBody.put("sources", List.of("GDS"));
        requestBody.put("searchCriteria", Map.of(
                "maxFlightOffers", 50,
                "flightFilters", Map.of(
                        "connectionRestriction", Map.of("maxNumberOfConnections", 2),
                        "cabinRestrictions", List.of(Map.of(
                                "cabin", "ECONOMY",
                                "coverage", "MOST_SEGMENTS",
                                "originDestinationIds", originDestinations.stream().map(d -> d.get("id")).toList()
                        ))
                )
        ));

        try {
            JsonNode response = client.post()
                    .uri("/v2/shopping/flight-offers")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();
            log.debug("Amadeus API 응답: {}", response.toPrettyString());
            if (response == null || !response.has("data")) {
                log.warn("Amadeus 응답에 데이터 없음");
                return new FlightSearchResDto(true, List.of());
            }

            return processFlightData(response.get("data"), origin, destination, isRoundTrip, reqDto);
        } catch (Exception e) {
            log.error("API 호출 실패: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    public FlightInfo getFlightDetail(String flightId) {
        log.info("항공편 상세 조회: flightId = {}", flightId);
        Optional<TravelFlight> travelFlightOpt = travelFlightRepository.findByFlightId(flightId);
        if (travelFlightOpt.isPresent()) {
            TravelFlight travelFlight = travelFlightOpt.get();
            FlightInfo flightInfo = new FlightInfo();
            flightInfo.setId(flightId);
            flightInfo.setTravelFlightId(travelFlight.getId());
            flightInfo.setCarrier(travelFlight.getAirlines());
            flightInfo.setCarrierCode(mapCarrierNameToCode(travelFlight.getAirlines()));
            flightInfo.setDepartureAirport(travelFlight.getDepartureAirport());
            flightInfo.setArrivalAirport(travelFlight.getArrivalAirport());
            flightInfo.setDepartureTime(travelFlight.getDepartureTime().toString());
            flightInfo.setArrivalTime(travelFlight.getArrivalTime().toString());
            flightInfo.setPrice(travelFlight.getPrice() != null ? travelFlight.getPrice() : "250000");
            flightInfo.setCurrency(travelFlight.getCurrency() != null ? travelFlight.getCurrency() : "KRW");
            flightInfo.setDuration(calculateDuration(travelFlight.getDepartureTime(), travelFlight.getArrivalTime()));
            flightInfo.setNumberOfBookableSeats(50);
            flightInfo.setFlightNumber(travelFlight.getFlightNumber());
            flightInfo.setCabinBaggage(travelFlight.getCabinBaggage() != null ? travelFlight.getCabinBaggage() : "Weight: 20kg");
            if (travelFlight.getReturnDepartureTime() != null) {
                flightInfo.setReturnDepartureTime(travelFlight.getReturnDepartureTime().toString());
                flightInfo.setReturnArrivalTime(travelFlight.getReturnArrivalTime().toString());
                flightInfo.setReturnDepartureAirport(travelFlight.getReturnDepartureAirport());
                flightInfo.setReturnArrivalAirport(travelFlight.getReturnArrivalAirport());
                flightInfo.setReturnDuration(calculateDuration(travelFlight.getReturnDepartureTime(), travelFlight.getReturnArrivalTime()));
                flightInfo.setReturnCarrier(travelFlight.getAirlines());
                flightInfo.setReturnCarrierCode(mapCarrierNameToCode(travelFlight.getAirlines()));
                flightInfo.setReturnFlightNumber(travelFlight.getFlightNumber());
            }
            log.info("DB에서 항공편 조회 성공: flightId = {}", flightId);
            return flightInfo;
        }

        log.info("DB에 항공편 없음, Amadeus API 호출 시도: flightId = {}", flightId);
        JsonNode flightOffer = fetchFlightOfferFromAmadeus(flightId);
        if (flightOffer == null) {
            log.warn("Amadeus에서 항공편을 찾을 수 없음: flightId = {}", flightId);
            throw new CustomException(ErrorCode.FLIGHT_NOT_FOUND, "항공편을 찾을 수 없습니다: flightId = " + flightId);
        }
        return processSingleFlightData(flightOffer);
    }

    public FlightInfo getFlightDetailByTravelFlightId(Long travelFlightId) {
        log.info("항공편 상세 조회 (TravelFlight ID): ID = {}", travelFlightId);
        Optional<TravelFlight> travelFlightOpt = travelFlightRepository.findById(travelFlightId);
        if (travelFlightOpt.isEmpty()) {
            log.warn("DB에서 항공편을 찾을 수 없음: TravelFlight ID = {}", travelFlightId);
            throw new CustomException(ErrorCode.FLIGHT_NOT_FOUND,
                    "항공편을 찾을 수 없습니다: TravelFlight ID = " + travelFlightId);
        }
        TravelFlight travelFlight = travelFlightOpt.get();
        log.debug("조회된 TravelFlight 데이터: id={}, flightId={}, airlines={}, departureTime={}",
                travelFlight.getId(), travelFlight.getFlightId(), travelFlight.getAirlines(),
                travelFlight.getDepartureTime());
        FlightInfo flightInfo = new FlightInfo();
        flightInfo.setId(travelFlight.getFlightId());
        flightInfo.setTravelFlightId(travelFlight.getId());
        flightInfo.setCarrier(travelFlight.getAirlines());
        flightInfo.setCarrierCode(mapCarrierNameToCode(travelFlight.getAirlines())); // 수정
        flightInfo.setFlightNumber(travelFlight.getFlightNumber());
        flightInfo.setDepartureAirport(travelFlight.getDepartureAirport());
        flightInfo.setArrivalAirport(travelFlight.getArrivalAirport());
        flightInfo.setDepartureTime(travelFlight.getDepartureTime().toString());
        flightInfo.setArrivalTime(travelFlight.getArrivalTime().toString());
        flightInfo.setDuration(calculateDuration(travelFlight.getDepartureTime(), travelFlight.getArrivalTime()));
        flightInfo.setPrice(travelFlight.getPrice().toString());
        flightInfo.setCurrency(travelFlight.getCurrency());
        flightInfo.setCabinBaggage(travelFlight.getCabinBaggage());
        flightInfo.setNumberOfBookableSeats(50);
        if (travelFlight.getReturnDepartureTime() != null) {
            flightInfo.setReturnDepartureTime(travelFlight.getReturnDepartureTime().toString());
            flightInfo.setReturnArrivalTime(travelFlight.getReturnArrivalTime().toString());
            flightInfo.setReturnDepartureAirport(travelFlight.getReturnDepartureAirport());
            flightInfo.setReturnArrivalAirport(travelFlight.getReturnArrivalAirport());
            flightInfo.setReturnDuration(calculateDuration(travelFlight.getReturnDepartureTime(), travelFlight.getReturnArrivalTime()));
            flightInfo.setReturnCarrier(travelFlight.getAirlines());
            flightInfo.setReturnCarrierCode(mapCarrierNameToCode(travelFlight.getAirlines())); // 수정
            flightInfo.setReturnFlightNumber(travelFlight.getFlightNumber());
            log.info("귀국 여정 설정 완료: {} -> {}, 시간: {} ~ {}",
                    flightInfo.getReturnDepartureAirport(), flightInfo.getReturnArrivalAirport(),
                    flightInfo.getReturnDepartureTime(), flightInfo.getReturnArrivalTime());
        }
        log.info("FlightInfo 생성 완료: flightId={}", flightInfo.getId());
        return flightInfo;
    }

    private JsonNode fetchFlightOfferFromAmadeus(String flightId) {
        log.warn("Amadeus API 호출 생략: flightId = {}는 유효하지 않을 가능성 높음", flightId);
        return null;
    }

    private FlightInfo processSingleFlightData(JsonNode flight) {
        log.info("단일 항공편 처리: ID = {}", flight.get("id").asText());
        String carrierCode = null;
        JsonNode itineraries = flight.get("itineraries");
        if (!itineraries.isArray() || itineraries.size() == 0) {
            log.warn("항공편 {} 제외: 유효하지 않은 여정 구조", flight.get("id").asText());
            return null;
        }

        JsonNode outbound = itineraries.get(0);
        JsonNode segments = outbound.get("segments");
        if (segments == null || segments.size() == 0) {
            log.warn("항공편 {} 제외: 유효하지 않은 출발 세그먼트", flight.get("id").asText());
            return null;
        }

        JsonNode firstSegment = segments.get(0);
        JsonNode lastSegment = segments.get(segments.size() - 1);
        String departureAirport = firstSegment.path("departure").path("iataCode").asText("");
        String arrivalAirport = lastSegment.path("arrival").path("iataCode").asText("");
        carrierCode = firstSegment.path("carrierCode").asText("Unknown");

        FlightInfo info = new FlightInfo();
        info.setId(flight.get("id").asText());
        info.setPrice(flight.path("price").path("total").asText(null));
        info.setCurrency(flight.path("price").path("currency").asText("KRW"));
        info.setNumberOfBookableSeats(flight.path("numberOfBookableSeats").asInt(0));
        info.setDuration(outbound.get("duration").asText(null));
        info.setCarrierCode(carrierCode);
        info.setCarrier(mapCarrierCodeToName(carrierCode));
        info.setFlightNumber(firstSegment.path("number").asText("Unknown"));
        info.setDepartureAirport(departureAirport);
        info.setArrivalAirport(arrivalAirport);
        info.setDepartureTime(firstSegment.path("departure").path("at").asText(""));
        info.setArrivalTime(lastSegment.path("arrival").path("at").asText(""));
        info.setAircraft(mapAircraftCode(firstSegment.path("aircraft").path("code").asText("Unknown")));

        String baggageInfo = "Unknown";
        if (flight.has("travelerPricings") && flight.get("travelerPricings").size() > 0) {
            JsonNode travelerPricing = flight.get("travelerPricings").get(0);
            if (travelerPricing.has("fareDetailsBySegment")) {
                JsonNode fareDetails = travelerPricing.get("fareDetailsBySegment").get(0);
                if (fareDetails.has("includedCheckedBags")) {
                    JsonNode baggage = fareDetails.get("includedCheckedBags");
                    baggageInfo = baggage.has("quantity") ? "Quantity: " + baggage.get("quantity").asInt() : "";
                    if (baggage.has("weight")) {
                        baggageInfo += (baggageInfo.isEmpty() ? "" : ", ") + "Weight: " + baggage.get("weight").asInt() + "kg";
                    }
                }
            }
        }
        info.setCabinBaggage(baggageInfo);

        if (itineraries.size() > 1) {
            JsonNode inbound = itineraries.get(1);
            JsonNode inboundSegments = inbound.get("segments");
            if (inboundSegments != null && inboundSegments.size() > 0) {
                JsonNode inboundFirstSegment = inboundSegments.get(0);
                JsonNode inboundLastSegment = inboundSegments.get(inboundSegments.size() - 1);
                info.setReturnDuration(inbound.get("duration").asText(null));
                info.setReturnCarrierCode(inboundFirstSegment.path("carrierCode").asText(carrierCode));
                info.setReturnCarrier(mapCarrierCodeToName(inboundFirstSegment.path("carrierCode").asText(carrierCode)));
                info.setReturnFlightNumber(inboundFirstSegment.path("number").asText("Unknown"));
                info.setReturnDepartureAirport(inboundFirstSegment.path("departure").path("iataCode").asText(null));
                info.setReturnArrivalAirport(inboundLastSegment.path("arrival").path("iataCode").asText(null));
                info.setReturnDepartureTime(inboundFirstSegment.path("departure").path("at").asText(""));
                info.setReturnArrivalTime(inboundLastSegment.path("arrival").path("at").asText(""));
                log.info("항공편 {} 귀국 여정 설정: {} -> {}, 시간: {} ~ {}",
                        flight.get("id").asText(), info.getReturnDepartureAirport(), info.getReturnArrivalAirport(),
                        info.getReturnDepartureTime(), info.getReturnArrivalTime());
            } else {
                log.warn("항공편 {} 귀국 여정 세그먼트 누락", flight.get("id").asText());
            }
        } else if (itineraries.size() == 1) {
            log.warn("항공편 {} 왕복 요청이지만 귀국 여정 누락", flight.get("id").asText());
        }

        if (info.getPrice() != null && info.getDepartureTime() != null && info.getArrivalTime() != null) {
            log.info("항공편 {} 처리 완료", flight.get("id").asText());
            return info;
        } else {
            log.warn("항공편 {} 제외: 필수 데이터 누락 (가격: {}, 출발: {}, 도착: {})",
                    flight.get("id").asText(), info.getPrice(), info.getDepartureTime(), info.getArrivalTime());
            return null;
        }
    }

    private FlightSearchResDto processFlightData(JsonNode data, String origin, String requestedDestination, boolean isRoundTrip, FlightSearchReqDto reqDto) {
        List<FlightInfo> results = new ArrayList<>();
        Map<String, String> carrierMap = getCarrierMap();
        Set<String> uniqueFlights = new HashSet<>();

        Map<String, Integer> minDurationMap = new HashMap<>();
        minDurationMap.put("JFK-CDG", 7);
        minDurationMap.put("CDG-JFK", 7);
        minDurationMap.put("LAX-JFK", 5);
        minDurationMap.put("JFK-LAX", 5);
        minDurationMap.put("CDG-NCE", 1);
        minDurationMap.put("CDG-LHR", 1);
        minDurationMap.put("LHR-CDG", 1);
        minDurationMap.put("CDG-FRA", 1);
        minDurationMap.put("CDG-MAD", 2);
        minDurationMap.put("CDG-YUL", 7);
        minDurationMap.put("CDG-KEF", 3);
        minDurationMap.put("CDG-LIS", 2);
        minDurationMap.put("CDG-IST", 3);
        minDurationMap.put("CDG-BEG", 2);
        minDurationMap.put("CDG-CMN", 3);

        for (JsonNode flight : data) {
            log.info("항공편 {} 처리 시작", flight.get("id").asText());
            boolean validItineraries = true;
            String carrierCode = null;
            String flightKey = null;

            if (!flight.has("itineraries") || !flight.get("itineraries").isArray()) {
                log.warn("항공편 {} 제외: 유효하지 않은 여정 구조", flight.get("id").asText());
                continue;
            }

            JsonNode itineraries = flight.get("itineraries");
            if (itineraries.size() < 1 || !itineraries.get(0).has("segments") || itineraries.get(0).get("segments").size() == 0) {
                log.warn("항공편 {} 제외: 유효하지 않은 출발 세그먼트", flight.get("id").asText());
                continue;
            }
            JsonNode outbound = itineraries.get(0);
            JsonNode segments = outbound.get("segments");
            JsonNode firstSegment = segments.get(0);
            JsonNode lastSegment = segments.get(segments.size() - 1);
            String departureAirport = firstSegment.path("departure").path("iataCode").asText("");
            String arrivalAirport = lastSegment.path("arrival").path("iataCode").asText("");
            carrierCode = firstSegment.path("carrierCode").asText("Unknown");

            if (!departureAirport.equals(origin) || !arrivalAirport.equals(requestedDestination)) {
                log.warn("항공편 {} 제외: 출발 경로 불일치, 실제: {} -> {}, 예상: {} -> {}",
                        flight.get("id").asText(), departureAirport, arrivalAirport, origin, requestedDestination);
                validItineraries = false;
            }

            if (!carrierMap.containsKey(carrierCode) && !carrierCode.equals("Unknown")) {
                log.warn("항공편 {} 제외: 유효하지 않은 항공사 코드 {}", flight.get("id").asText(), carrierCode);
                validItineraries = false;
            }

            String routeKey = departureAirport + "-" + arrivalAirport;
            if (outbound.has("duration")) {
                try {
                    Duration duration = Duration.parse(outbound.get("duration").asText());
                    int minHours = minDurationMap.getOrDefault(routeKey, 1);
                    log.info("항공편 {} 출발 소요 시간: {} (최소: {}시간)", flight.get("id").asText(), duration, minHours);
                    if (duration.toHours() < minHours) {
                        log.warn("항공편 {} 제외: 출발 소요 시간 {} 미만, 경로 {}",
                                flight.get("id").asText(), duration, routeKey);
                        validItineraries = false;
                    }
                } catch (Exception e) {
                    log.warn("항공편 {} 제외: 유효하지 않은 출발 소요 시간 형식: {}",
                            flight.get("id").asText(), outbound.get("duration").asText());
                    validItineraries = false;
                }
            }

            JsonNode inbound = null;
            if (isRoundTrip && itineraries.size() > 1) {
                inbound = itineraries.get(1);
                if (!inbound.has("segments") || inbound.get("segments").size() == 0) {
                    log.warn("항공편 {} 제외: 유효하지 않은 귀국 세그먼트", flight.get("id").asText());
                    validItineraries = false;
                } else {
                    JsonNode inboundSegments = inbound.get("segments");
                    JsonNode inboundFirstSegment = inboundSegments.get(0);
                    JsonNode inboundLastSegment = inboundSegments.get(inboundSegments.size() - 1);
                    String returnDepartureAirport = inboundFirstSegment.path("departure").path("iataCode").asText("");
                    String returnArrivalAirport = inboundLastSegment.path("arrival").path("iataCode").asText("");
                    String inboundCarrierCode = inboundFirstSegment.path("carrierCode").asText("Unknown");

                    if (!returnDepartureAirport.equals(requestedDestination) || !returnArrivalAirport.equals(origin)) {
                        log.warn("항공편 {} 제외: 귀국 경로 불일치, 실제: {} -> {}, 예상: {} -> {}",
                                flight.get("id").asText(), returnDepartureAirport, returnArrivalAirport, requestedDestination, origin);
                        validItineraries = false;
                    }

                    String returnRouteKey = returnDepartureAirport + "-" + returnArrivalAirport;
                    if (inbound.has("duration")) {
                        try {
                            Duration duration = Duration.parse(inbound.get("duration").asText());
                            int minHours = minDurationMap.getOrDefault(returnRouteKey, 1);
                            log.info("항공편 {} 귀국 소요 시간: {} (최소: {}시간)", flight.get("id").asText(), duration, minHours);
                            if (duration.toHours() < minHours) {
                                log.warn("항공편 {} 제외: 귀국 소요 시간 {} 미만, 경로 {}",
                                        flight.get("id").asText(), duration, returnRouteKey);
                                validItineraries = false;
                            }
                        } catch (Exception e) {
                            log.warn("항공편 {} 제외: 유효하지 않은 귀국 소요 시간 형식: {}",
                                    flight.get("id").asText(), inbound.get("duration").asText());
                            validItineraries = false;
                        }
                    }
                }
            } else if (isRoundTrip) {
                log.warn("항공편 {} 귀국 여정 누락: 여정 수 {}. API 응답 여정: {}",
                        flight.get("id").asText(), itineraries.size(), itineraries.toPrettyString());
                validItineraries = false;
            }

            if (!validItineraries) {
                log.info("항공편 {} 제외: 유효하지 않은 여정", flight.get("id").asText());
                continue;
            }

            String departureTime = firstSegment.path("departure").path("at").asText("");
            String arrivalTime = lastSegment.path("arrival").path("at").asText("");
            String price = flight.path("price").path("total").asText("");
            flightKey = carrierCode + "-" + departureTime + "-" + arrivalTime + "-" + price;
            String returnDepartureTime = "";
            String returnArrivalTime = "";
            if (inbound != null && inbound.get("segments").size() > 0) {
                JsonNode inboundSegments = inbound.get("segments");
                JsonNode inboundFirstSegment = inboundSegments.get(0);
                JsonNode inboundLastSegment = inboundSegments.get(inboundSegments.size() - 1);
                returnDepartureTime = inboundFirstSegment.path("departure").path("at").asText("");
                returnArrivalTime = inboundLastSegment.path("arrival").path("at").asText("");
                flightKey += "-" + returnDepartureTime + "-" + returnArrivalTime;
            }
            if (!uniqueFlights.add(flightKey)) {
                log.warn("항공편 {} 제외: 중복 항공편 {}", flight.get("id").asText(), flightKey);
                continue;
            }

            FlightInfo info = new FlightInfo();
            info.setId(flight.get("id").asText());
            info.setPrice(flight.path("price").path("total").asText(null));
            info.setCurrency(flight.path("price").path("currency").asText("KRW"));
            info.setNumberOfBookableSeats(flight.path("numberOfBookableSeats").asInt(0));
            info.setDuration(outbound.get("duration").asText(null));
            info.setCarrierCode(carrierCode);
            info.setCarrier(mapCarrierCodeToName(carrierCode));
            info.setFlightNumber(firstSegment.path("number").asText("Unknown"));
            info.setDepartureAirport(departureAirport);
            info.setArrivalAirport(arrivalAirport);
            info.setDepartureTime(departureTime);
            info.setArrivalTime(arrivalTime);
            info.setAircraft(mapAircraftCode(firstSegment.path("aircraft").path("code").asText("Unknown")));

            if (inbound != null && inbound.get("segments").size() > 0) {
                JsonNode inboundSegments = inbound.get("segments");
                JsonNode inboundFirstSegment = inboundSegments.get(0);
                JsonNode inboundLastSegment = inboundSegments.get(inboundSegments.size() - 1);
                info.setReturnDuration(inbound.get("duration").asText(null));
                info.setReturnCarrierCode(inboundFirstSegment.path("carrierCode").asText(carrierCode));
                info.setReturnCarrier(mapCarrierCodeToName(inboundFirstSegment.path("carrierCode").asText(carrierCode)));
                info.setReturnFlightNumber(inboundFirstSegment.path("number").asText("Unknown"));
                info.setReturnDepartureAirport(inboundFirstSegment.path("departure").path("iataCode").asText(null));
                info.setReturnArrivalAirport(inboundLastSegment.path("arrival").path("iataCode").asText(null));
                info.setReturnDepartureTime(returnDepartureTime);
                info.setReturnArrivalTime(returnArrivalTime);
                log.info("항공편 {} 귀국 여정 설정: {} -> {}, 시간: {} ~ {}",
                        flight.get("id").asText(), info.getReturnDepartureAirport(), info.getReturnArrivalAirport(),
                        returnDepartureTime, returnArrivalTime);
            }

            String baggageInfo = "Unknown";
            if (flight.has("travelerPricings") && flight.get("travelerPricings").size() > 0) {
                JsonNode travelerPricing = flight.get("travelerPricings").get(0);
                if (travelerPricing.has("fareDetailsBySegment")) {
                    JsonNode fareDetails = travelerPricing.get("fareDetailsBySegment").get(0);
                    if (fareDetails.has("includedCheckedBags")) {
                        JsonNode baggage = fareDetails.get("includedCheckedBags");
                        baggageInfo = baggage.has("quantity") ? "Quantity: " + baggage.get("quantity").asInt() : "";
                        if (baggage.has("weight")) {
                            baggageInfo += (baggageInfo.isEmpty() ? "" : ", ") + "Weight: " + baggage.get("weight").asInt() + "kg";
                        }
                    }
                }
            }
            info.setCabinBaggage(baggageInfo);

            Long travelFlightId = saveFlightForSearch(info, reqDto);
            info.setTravelFlightId(travelFlightId);

            if (info.getPrice() != null && info.getDepartureTime() != null && info.getArrivalTime() != null) {
                results.add(info);
                log.info("항공편 {} 추가: 출발 {}, 귀국 {}, travelFlightId={}",
                        flight.get("id").asText(), info.getDuration(), info.getReturnDuration(), info.getTravelFlightId());
            } else {
                log.warn("항공편 {} 제외: 필수 데이터 누락 (가격: {}, 출발: {}, 도착: {})",
                        flight.get("id").asText(), info.getPrice(), info.getDepartureTime(), info.getArrivalTime());
            }
        }

        if (isRoundTrip && results.stream().noneMatch(f -> f.getReturnDepartureTime() != null)) {
            log.error("왕복 요청이지만 귀국 여정 데이터 없음. 반환된 항공편 수: {}", results.size());
            throw new CustomException(ErrorCode.INVALID_FLIGHT_SEARCH, "왕복 항공편의 귀국 여정 데이터가 누락되었습니다.");
        }

        FlightSearchResDto resDto = new FlightSearchResDto();
        resDto.setSuccess(true);
        resDto.setFlights(results);
        log.info("목적지 {}에 대해 {}개의 항공편 반환", requestedDestination, results.size());
        return resDto;
    }

    private Long saveFlightForSearch(FlightInfo flightInfo, FlightSearchReqDto reqDto) {
        log.info("항공편 저장 요청: flightId={}", flightInfo.getId());
        Optional<TravelPlan> travelPlanOpt = travelPlanRepository.findFirstByOrderByIdDesc();
        if (travelPlanOpt.isEmpty()) {
            log.warn("여행 계획 없음, 기본 여행 계획 생성");
            TravelPlan travelPlan = new TravelPlan();
            travelPlan.setCreatedAt(LocalDateTime.now());
            travelPlan.setUpdatedAt(LocalDateTime.now());
            travelPlan = travelPlanRepository.save(travelPlan);
        }

        TravelPlan travelPlan = travelPlanOpt.orElseGet(() -> {
            TravelPlan newPlan = new TravelPlan();
            newPlan.setCreatedAt(LocalDateTime.now());
            newPlan.setUpdatedAt(LocalDateTime.now());
            return travelPlanRepository.save(newPlan);
        });

        TravelFlight travelFlight = new TravelFlight();
        travelFlight.setTravelPlan(travelPlan);
        travelFlight.setFlightId(flightInfo.getId());
        travelFlight.setAirlines(flightInfo.getCarrier());
        travelFlight.setFlightNumber(flightInfo.getFlightNumber());
        travelFlight.setDepartureAirport(flightInfo.getDepartureAirport());
        travelFlight.setArrivalAirport(flightInfo.getArrivalAirport());
        try {
            travelFlight.setDepartureTime(LocalDateTime.parse(flightInfo.getDepartureTime()));
            travelFlight.setArrivalTime(LocalDateTime.parse(flightInfo.getArrivalTime()));
        } catch (DateTimeParseException e) {
            log.error("날짜 파싱 오류: departureTime={}, arrivalTime={}",
                    flightInfo.getDepartureTime(), flightInfo.getArrivalTime());
            throw new CustomException(ErrorCode.INVALID_FLIGHT_SEARCH);
        }
        if (flightInfo.getReturnDepartureTime() != null) {
            try {
                travelFlight.setReturnDepartureTime(LocalDateTime.parse(flightInfo.getReturnDepartureTime()));
                travelFlight.setReturnArrivalTime(LocalDateTime.parse(flightInfo.getReturnArrivalTime()));
                travelFlight.setReturnDepartureAirport(flightInfo.getReturnDepartureAirport());
                travelFlight.setReturnArrivalAirport(flightInfo.getReturnArrivalAirport());
            } catch (DateTimeParseException e) {
                log.error("귀국 여정 날짜 파싱 오류: returnDepartureTime={}, returnArrivalTime={}",
                        flightInfo.getReturnDepartureTime(), flightInfo.getReturnArrivalTime());
                throw new CustomException(ErrorCode.INVALID_FLIGHT_SEARCH);
            }
        }
        travelFlight.setPrice(flightInfo.getPrice());
        travelFlight.setCurrency(flightInfo.getCurrency());
        travelFlight.setCabinBaggage(flightInfo.getCabinBaggage());
        travelFlight.setCreatedAt(LocalDateTime.now());
        travelFlight.setUpdatedAt(LocalDateTime.now());

        travelFlight = travelFlightRepository.save(travelFlight);
        log.info("항공편 저장 성공: id={}, flightId={}", travelFlight.getId(), travelFlight.getFlightId());
        return travelFlight.getId();
    }

    public Long saveFlight(FlightSearchReqDto reqDto, Long travelPlanId) {
        log.info("항공편 저장 요청: {}, travelPlanId: {}", reqDto, travelPlanId);
        FlightSearchResDto result = searchFlights(reqDto);
        if (result.getFlights().isEmpty()) {
            log.error("항공편 검색 결과 없음: {}", reqDto);
            throw new CustomException(ErrorCode.INVALID_FLIGHT_SEARCH);
        }

        TravelPlan travelPlan = travelPlanRepository.findById(travelPlanId)
                .orElseThrow(() -> {
                    log.error("여행 계획 없음: {}", travelPlanId);
                    return new CustomException(ErrorCode.INVALID_FLIGHT_SEARCH);
                });

        FlightInfo flightInfo = result.getFlights().get(0);
        TravelFlight travelFlight = new TravelFlight();
        travelFlight.setTravelPlan(travelPlan);
        travelFlight.setFlightId(flightInfo.getId());
        travelFlight.setAirlines(flightInfo.getCarrier());
        travelFlight.setFlightNumber(flightInfo.getFlightNumber());
        travelFlight.setDepartureAirport(flightInfo.getDepartureAirport());
        travelFlight.setArrivalAirport(flightInfo.getArrivalAirport());
        try {
            travelFlight.setDepartureTime(LocalDateTime.parse(flightInfo.getDepartureTime()));
            travelFlight.setArrivalTime(LocalDateTime.parse(flightInfo.getArrivalTime()));
        } catch (DateTimeParseException e) {
            log.error("날짜 파싱 오류: departureTime={}, arrivalTime={}",
                    flightInfo.getDepartureTime(), flightInfo.getArrivalTime());
            throw new CustomException(ErrorCode.INVALID_FLIGHT_SEARCH);
        }
        if (flightInfo.getReturnDepartureTime() != null) {
            try {
                travelFlight.setReturnDepartureTime(LocalDateTime.parse(flightInfo.getReturnDepartureTime()));
                travelFlight.setReturnArrivalTime(LocalDateTime.parse(flightInfo.getReturnArrivalTime()));
                travelFlight.setReturnDepartureAirport(flightInfo.getReturnDepartureAirport());
                travelFlight.setReturnArrivalAirport(flightInfo.getReturnArrivalAirport());
            } catch (DateTimeParseException e) {
                log.error("귀국 여정 날짜 파싱 오류: returnDepartureTime={}, returnArrivalTime={}",
                        flightInfo.getReturnDepartureTime(), flightInfo.getReturnArrivalTime());
                throw new CustomException(ErrorCode.INVALID_FLIGHT_SEARCH);
            }
        }
        travelFlight.setPrice(flightInfo.getPrice());
        travelFlight.setCurrency(flightInfo.getCurrency());
        travelFlight.setCabinBaggage(flightInfo.getCabinBaggage());
        travelFlight.setCreatedAt(LocalDateTime.now());
        travelFlight.setUpdatedAt(LocalDateTime.now());

        travelFlight = travelFlightRepository.save(travelFlight);
        log.info("항공편 저장 성공: id = {}, flightId = {}", travelFlight.getId(), travelFlight.getFlightId());
        return travelFlight.getId();
    }

    @CacheEvict(value = "flights", key = "#reqDto.origin + '-' + #reqDto.destination + '-' + #reqDto.departureDate + '-' + (#reqDto.returnDate != null ? #reqDto.returnDate : 'none')")
    public void clearFlightCache(FlightSearchReqDto reqDto) {
        log.info("항공편 캐시 삭제 요청: {}", reqDto);
    }

    private void validateSearchRequest(FlightSearchReqDto reqDto) {
        if (reqDto.getOrigin() == null || reqDto.getOrigin().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_FLIGHT_SEARCH, "출발지가 필요합니다.");
        }
        if (reqDto.getDestination() == null || reqDto.getDestination().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_FLIGHT_SEARCH, "목적지가 필요합니다.");
        }
        if (reqDto.getDepartureDate() == null || reqDto.getDepartureDate().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_FLIGHT_SEARCH, "출발일이 필요합니다.");
        }
        try {
            LocalDate.parse(reqDto.getDepartureDate());
        } catch (DateTimeParseException e) {
            throw new CustomException(ErrorCode.INVALID_FLIGHT_SEARCH, "유효하지 않은 출발일 형식입니다.");
        }
        if (reqDto.getReturnDate() != null && !reqDto.getReturnDate().isEmpty()) {
            try {
                LocalDate.parse(reqDto.getReturnDate());
            } catch (DateTimeParseException e) {
                throw new CustomException(ErrorCode.INVALID_FLIGHT_SEARCH, "유효하지 않은 귀국일 형식입니다.");
            }
            if (LocalDate.parse(reqDto.getReturnDate()).isBefore(LocalDate.parse(reqDto.getDepartureDate()))) {
                throw new CustomException(ErrorCode.INVALID_FLIGHT_SEARCH, "귀국일은 출발일 이후여야 합니다.");
            }
        }
    }

    private String calculateDuration(LocalDateTime departure, LocalDateTime arrival) {
        if (departure == null || arrival == null) return "N/A";
        Duration duration = Duration.between(departure, arrival);
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        return String.format("PT%dH%dM", hours, minutes);
    }

    private String mapCarrierNameToCode(String carrierName) {
        Map<String, String> carrierNameToCodeMap = new HashMap<>();
        carrierNameToCodeMap.put("대한항공", "KE");
        carrierNameToCodeMap.put("아시아나항공", "OZ");
        carrierNameToCodeMap.put("Air France", "AF");
        carrierNameToCodeMap.put("Delta Air Lines", "DL");
        carrierNameToCodeMap.put("Lufthansa", "LH");
        carrierNameToCodeMap.put("Iberia", "IB");
        carrierNameToCodeMap.put("United Airlines", "UA");
        carrierNameToCodeMap.put("British Airways", "BA");
        carrierNameToCodeMap.put("Icelandair", "FI");
        carrierNameToCodeMap.put("Turkish Airlines", "TK");
        carrierNameToCodeMap.put("Air Serbia", "JU");
        carrierNameToCodeMap.put("Royal Air Maroc", "AT");
        carrierNameToCodeMap.put("West Air Sweden", "6X");
        carrierNameToCodeMap.put("Frontier Airlines", "F9");
        carrierNameToCodeMap.put("JetBlue Airways", "B6");
        return carrierNameToCodeMap.getOrDefault(carrierName, "Unknown");
    }

    private String mapCarrierCodeToName(String carrierCode) {
        Map<String, String> carrierMap = getCarrierMap();
        return carrierMap.getOrDefault(carrierCode, "Unknown Airline");
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
        carrierMap.put("6X", "West Air Sweden");
        carrierMap.put("F9", "Frontier Airlines");
        carrierMap.put("B6", "JetBlue Airways");
        return carrierMap;
    }

    private String mapAircraftCode(String aircraftCode) {
        Map<String, String> aircraftMap = new HashMap<>();
        aircraftMap.put("320", "Airbus A320");
        aircraftMap.put("321", "Airbus A321");
        aircraftMap.put("330", "Airbus A330");
        aircraftMap.put("350", "Airbus A350");
        aircraftMap.put("380", "Airbus A380");
        aircraftMap.put("737", "Boeing 737");
        aircraftMap.put("747", "Boeing 747");
        aircraftMap.put("757", "Boeing 757");
        aircraftMap.put("767", "Boeing 767");
        aircraftMap.put("777", "Boeing 777");
        aircraftMap.put("787", "Boeing 787");
        return aircraftMap.getOrDefault(aircraftCode, "Unknown Aircraft");
    }
}