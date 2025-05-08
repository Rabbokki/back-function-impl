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

    @Cacheable(
            value = "flights",
            key = "#reqDto.origin + '-' + #reqDto.destination + '-' + #reqDto.departureDate + '-' + (#reqDto.returnDate != null ? #reqDto.returnDate : 'none') + '-' + #reqDto.realTime",
            unless = "#reqDto.realTime"
    )
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
        requestBody.put("currencyCode", "USD");

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

            if (response == null || !response.has("data")) {
                log.warn("Amadeus 응답에 데이터 없음");
                return new FlightSearchResDto(true, List.of());
            }

            return processFlightData(response.get("data"), origin, destination, isRoundTrip);
        } catch (Exception e) {
            log.error("API 호출 실패: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private FlightSearchResDto processFlightData(JsonNode data, String origin, String requestedDestination, boolean isRoundTrip) {
        List<FlightInfo> results = new ArrayList<>();
        Map<String, String> carrierMap = getCarrierMap();
        Set<String> uniqueFlights = new HashSet<>();

        Map<String, Integer> minDurationMap = new HashMap<>();
        minDurationMap.put("CDG-JFK", 7);
        minDurationMap.put("JFK-CDG", 7);
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
            log.info("항공편 {} 여정 수: {}", flight.get("id").asText(), itineraries.size());
            log.debug("항공편 {} 여정 구조: {}", flight.get("id").asText(), itineraries.toPrettyString());

            // 출발 여정
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
                log.info("항공편 {} 알 수 없는 항공사 코드 {}, 포함됨", flight.get("id").asText(), carrierCode);
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

            // 귀국 여정
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

            // 중복 체크
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

            // FlightInfo 생성
            FlightInfo info = new FlightInfo();
            info.setId(flight.get("id").asText());
            info.setPrice(flight.path("price").path("total").asText(null));
            info.setCurrency(flight.path("price").path("currency").asText("USD"));
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

            // 귀국 여정 데이터 설정
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

            // 수하물 정보
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

            if (info.getPrice() != null && info.getDepartureTime() != null && info.getArrivalTime() != null) {
                results.add(info);
                log.info("항공편 {} 추가: 출발 {}, 귀국 {}", flight.get("id").asText(),
                        info.getDuration(), info.getReturnDuration());
            } else {
                log.warn("항공편 {} 제외: 필수 데이터 누락 (가격: {}, 출발: {}, 도착: {})",
                        flight.get("id").asText(), info.getPrice(), info.getDepartureTime(), info.getArrivalTime());
            }
        }

        if (isRoundTrip && results.stream().noneMatch(f -> f.getReturnDepartureTime() != null)) {
            log.warn("왕복 요청이지만 귀국 여정 데이터 없음. 반환된 항공편 수: {}", results.size());
        }

        FlightSearchResDto resDto = new FlightSearchResDto();
        resDto.setSuccess(true);
        resDto.setFlights(results);
        log.info("목적지 {}에 대해 {}개의 항공편 반환", requestedDestination, results.size());
        return resDto;
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
        travelFlight.setAirlines(flightInfo.getCarrier());
        travelFlight.setDepartureAirport(flightInfo.getDepartureAirport());
        travelFlight.setArrivalAirport(flightInfo.getArrivalAirport());
        travelFlight.setDepartureTime(LocalDateTime.parse(flightInfo.getDepartureTime()));
        travelFlight.setArrivalTime(LocalDateTime.parse(flightInfo.getArrivalTime()));

        travelFlight = travelFlightRepository.save(travelFlight);
        log.info("항공편 저장 성공, ID: {}", travelFlight.getId());
        return travelFlight.getId();
    }

    @CacheEvict(value = "flights", key = "#reqDto.origin + '-' + #reqDto.destination + '-' + #reqDto.departureDate")
    public void clearFlightCache(FlightSearchReqDto reqDto) {
        log.info("항공편 캐시 삭제 요청: {}", reqDto);
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

    private String mapCarrierCodeToName(String carrierCode) {
        return getCarrierMap().getOrDefault(carrierCode, carrierCode);
    }

    private String mapAircraftCode(String aircraftCode) {
        Map<String, String> aircraftMap = new HashMap<>();
        aircraftMap.put("789", "Boeing 787-9");
        aircraftMap.put("380", "Airbus A380");
        aircraftMap.put("737", "Boeing 737");
        aircraftMap.put("738", "Boeing 737-800");
        aircraftMap.put("320", "Airbus A320");
        aircraftMap.put("767", "Boeing 767");
        aircraftMap.put("223", "Airbus A220-300");
        aircraftMap.put("772", "Boeing 777-200");
        aircraftMap.put("77W", "Boeing 777-300ER");
        aircraftMap.put("359", "Airbus A350-900");
        aircraftMap.put("343", "Airbus A340-300");
        aircraftMap.put("744", "Boeing 747-400");
        aircraftMap.put("32Q", "Airbus A321neo");
        aircraftMap.put("32N", "Airbus A320neo");
        return aircraftMap.getOrDefault(aircraftCode, aircraftCode);
    }

    private void validateSearchRequest(FlightSearchReqDto reqDto) {
        log.debug("검색 요청 검증: {}", reqDto);
        if (reqDto.getOrigin() == null || reqDto.getOrigin().isEmpty() ||
                reqDto.getDestination() == null || reqDto.getDestination().isEmpty() ||
                reqDto.getDepartureDate() == null || reqDto.getDepartureDate().isEmpty()) {
            log.error("필수 필드 누락으로 검색 요청 유효하지 않음");
            throw new CustomException(ErrorCode.INVALID_FLIGHT_SEARCH);
        }
        try {
            LocalDate departureDate = LocalDate.parse(reqDto.getDepartureDate());
            LocalDate maxDate = LocalDate.now().plusDays(330);
            if (departureDate.isAfter(maxDate)) {
                log.error("출발 날짜 {}가 최대 허용 날짜 {}를 초과함", departureDate, maxDate);
                throw new CustomException(ErrorCode.INVALID_FLIGHT_SEARCH);
            }
            if (reqDto.getReturnDate() != null && !reqDto.getReturnDate().isEmpty()) {
                LocalDate returnDate = LocalDate.parse(reqDto.getReturnDate());
                if (returnDate.isBefore(departureDate)) {
                    log.error("귀국 날짜 {}가 출발 날짜 {}보다 빠름", returnDate, departureDate);
                    throw new CustomException(ErrorCode.INVALID_FLIGHT_SEARCH);
                }
            }
        } catch (DateTimeParseException e) {
            log.error("날짜 형식 오류: departureDate={}, returnDate={}", reqDto.getDepartureDate(), reqDto.getReturnDate());
            throw new CustomException(ErrorCode.INVALID_FLIGHT_SEARCH);
        }
    }
}