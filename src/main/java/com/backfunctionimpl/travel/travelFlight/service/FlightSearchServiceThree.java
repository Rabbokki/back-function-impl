package com.backfunctionimpl.travel.travelFlight.service;

import com.backfunctionimpl.global.error.CustomException;
import com.backfunctionimpl.global.error.ErrorCode;
import com.backfunctionimpl.travel.config.AmadeusClient;
import com.backfunctionimpl.travel.travelFlight.data.MockFlightData;
import com.backfunctionimpl.travel.travelFlight.dto.FlightInfo;
import com.backfunctionimpl.travel.travelFlight.dto.FlightSearchReqDto;
import com.backfunctionimpl.travel.travelFlight.dto.FlightSearchResDto;
import com.backfunctionimpl.travel.travelFlight.entity.TravelFlight;
import com.backfunctionimpl.travel.travelFlight.repository.TravelFlightRepository;
import com.backfunctionimpl.travel.travelPlan.entity.TravelPlan;
import com.backfunctionimpl.travel.travelPlan.repository.TravelPlanRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
@Slf4j
public class FlightSearchServiceThree {
    private final AmadeusClient amadeusClient;
    private final TravelFlightRepository travelFlightRepository;
    private final TravelPlanRepository travelPlanRepository;
    private final ObjectMapper objectMapper;

    public FlightSearchServiceThree(AmadeusClient amadeusClient, TravelFlightRepository travelFlightRepository,
                                  TravelPlanRepository travelPlanRepository) {
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
        String departureDate = reqDto.getDepartureDate();
        String returnDate = reqDto.getReturnDate();
        boolean isRoundTrip = reqDto.isRealTime() && returnDate != null && !returnDate.isEmpty();

        try {
            // MockFlightData에서 항공편 가져오기
            List<FlightInfo> flights = MockFlightData.getFlights(origin, destination, departureDate, returnDate);
            log.info("Mock 데이터에서 {}개의 항공편 반환", flights.size());

            // 저장 및 travelFlightId 설정
            List<FlightInfo> savedFlights = new ArrayList<>();
            for (FlightInfo flight : flights) {
                Long travelFlightId = saveFlightForSearch(flight, reqDto);
                flight.setTravelFlightId(travelFlightId);
                savedFlights.add(flight);
            }

            if (isRoundTrip && savedFlights.stream().noneMatch(f -> f.getReturnDepartureTime() != null)) {
                log.warn("왕복 요청이지만 귀국 여정 데이터 없음. 반환된 항공편 수: {}", savedFlights.size());
            }

            return FlightSearchResDto.builder()
                    .success(true)
                    .flights(savedFlights)
                    .build();
        } catch (Exception e) {
            log.error("항공편 검색 실패: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "항공편 검색에 실패했습니다.");
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

        log.warn("DB에 항공편 없음: flightId = {}", flightId);
        throw new CustomException(ErrorCode.FLIGHT_NOT_FOUND, "항공편을 찾을 수 없습니다: flightId = " + flightId);
    }

    public FlightInfo getFlightDetailByTravelFlightId(Long travelFlightId) {
        log.info("항공편 상세 조회 (TravelFlight ID): ID = {}", travelFlightId);
        Optional<TravelFlight> travelFlightOpt = travelFlightRepository.findById(travelFlightId);
        if (travelFlightOpt.isEmpty()) {
            log.warn("DB에서 항공편을 찾을 수 없음: TravelFlight ID = {}", travelFlightId);
            throw new CustomException(ErrorCode.FLIGHT_NOT_FOUND, "항공편을 찾을 수 없습니다: TravelFlight ID = " + travelFlightId);
        }
        TravelFlight travelFlight = travelFlightOpt.get();
        log.debug("조회된 TravelFlight 데이터: id={}, flightId={}, airlines={}, departureTime={}",
                travelFlight.getId(), travelFlight.getFlightId(), travelFlight.getAirlines(),
                travelFlight.getDepartureTime());
        FlightInfo flightInfo = new FlightInfo();
        flightInfo.setId(travelFlight.getFlightId());
        flightInfo.setTravelFlightId(travelFlight.getId());
        flightInfo.setCarrier(travelFlight.getAirlines());
        flightInfo.setCarrierCode(mapCarrierNameToCode(travelFlight.getAirlines()));
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
            flightInfo.setReturnCarrierCode(mapCarrierNameToCode(travelFlight.getAirlines()));
            flightInfo.setReturnFlightNumber(travelFlight.getFlightNumber());
            log.info("귀국 여정 설정 완료: {} -> {}, 시간: {} ~ {}",
                    flightInfo.getReturnDepartureAirport(), flightInfo.getReturnArrivalAirport(),
                    flightInfo.getReturnDepartureTime(), flightInfo.getReturnArrivalTime());
        }
        log.info("FlightInfo 생성 완료: flightId={}", flightInfo.getId());
        return flightInfo;
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
        carrierNameToCodeMap.put("British Airways", "BA");
        return carrierNameToCodeMap.getOrDefault(carrierName, "Unknown");
    }

    private String mapCarrierCodeToName(String carrierCode) {
        Map<String, String> carrierMap = new HashMap<>();
        carrierMap.put("KE", "대한항공");
        carrierMap.put("OZ", "아시아나항공");
        carrierMap.put("AF", "Air France");
        carrierMap.put("DL", "Delta Air Lines");
        carrierMap.put("BA", "British Airways");
        return carrierMap.getOrDefault(carrierCode, "Unknown Airline");
    }
}