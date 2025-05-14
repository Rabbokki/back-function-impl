package com.backfunctionimpl.travel.travelFlight.service;

import com.backfunctionimpl.account.entity.Account;
import com.backfunctionimpl.account.repository.AccountRepository;
import com.backfunctionimpl.global.error.CustomException;
import com.backfunctionimpl.global.error.ErrorCode;
import com.backfunctionimpl.travel.config.AmadeusClient;
import com.backfunctionimpl.travel.travelFlight.data.MockFlightData;
import com.backfunctionimpl.travel.travelFlight.dto.*;
import com.backfunctionimpl.travel.travelFlight.entity.AccountFlight;
import com.backfunctionimpl.travel.travelFlight.entity.TravelFlight;
import com.backfunctionimpl.travel.travelFlight.repository.AccountFlightRepository;
import com.backfunctionimpl.travel.travelFlight.repository.TravelFlightRepository;
import com.backfunctionimpl.travel.travelPlan.entity.TravelPlan;
import com.backfunctionimpl.travel.travelPlan.repository.TravelPlanRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FlightSearchServiceThree {
    private final AmadeusClient amadeusClient;
    private final TravelFlightRepository travelFlightRepository;
    private final AccountFlightRepository accountFlightRepository;
    private final AccountRepository accountRepository;
    private final TravelPlanRepository travelPlanRepository;
    private final ObjectMapper objectMapper;

    public FlightSearchServiceThree(AmadeusClient amadeusClient, TravelFlightRepository travelFlightRepository,
                                    AccountRepository accountRepository,
                                    AccountFlightRepository accountFlightRepository,
                                    TravelPlanRepository travelPlanRepository) {
        this.amadeusClient = amadeusClient;
        this.travelFlightRepository = travelFlightRepository;
        this.accountRepository = accountRepository;
        this.accountFlightRepository = accountFlightRepository;
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
            long startTime = System.currentTimeMillis();
            List<FlightInfo> flights = MockFlightData.getFlights(origin, destination, departureDate, returnDate);
            log.info("Mock 데이터에서 {}개의 항공편 반환, 소요 시간: {}ms", flights.size(), System.currentTimeMillis() - startTime);

            if (flights.isEmpty()) {
                log.warn("항공편 검색 결과 없음: origin={}, destination={}, departureDate={}, returnDate={}",
                        origin, destination, departureDate, returnDate);
                throw new CustomException(ErrorCode.FLIGHT_NOT_FOUND, "검색된 항공편이 없습니다. 다른 경로 또는 날짜를 시도해주세요.");
            }

            List<FlightInfo> savedFlights = new ArrayList<>();
            startTime = System.currentTimeMillis();
            for (FlightInfo flight : flights) {
                Long travelFlightId = saveFlightForSearch(flight, reqDto);
                flight.setTravelFlightId(travelFlightId);
                savedFlights.add(flight);
            }
            log.info("항공편 저장 완료, 소요 시간: {}ms", System.currentTimeMillis() - startTime);

            if (isRoundTrip && savedFlights.stream().noneMatch(f -> f.getReturnDepartureTime() != null)) {
                log.warn("왕복 요청이지만 귀국 여정 데이터 없음: origin={}, destination={}, returnDate={}",
                        origin, destination, returnDate);
                throw new CustomException(ErrorCode.FLIGHT_NOT_FOUND, "귀국 여정을 찾을 수 없습니다. 다른 날짜를 시도해주세요.");
            }

            return FlightSearchResDto.builder()
                    .success(true)
                    .flights(savedFlights)
                    .build();
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("항공편 검색 실패: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "항공편 검색에 실패했습니다: " + e.getMessage());
        }
    }

    public FlightInfo getFlightDetail(String flightId) {
        log.info("항공편 상세 조회: flightId = {}", flightId);
        Optional<TravelFlight> travelFlightOpt = travelFlightRepository.findByFlightId(flightId);
        if (travelFlightOpt.isPresent()) {
            TravelFlight travelFlight = travelFlightOpt.get();
            FlightInfo flightInfo = buildFlightInfo(travelFlight);
            log.info("DB에서 항공편 조회 성공: flightId = {}, travelFlightId = {}", flightId, flightInfo.getTravelFlightId());
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
            List<TravelFlight> allFlights = travelFlightRepository.findAll();
            log.debug("현재 TravelFlight 테이블 상태: {}개 레코드", allFlights.size());
            allFlights.forEach(flight -> log.debug("TravelFlight: id={}, flightId={}", flight.getId(), flight.getFlightId()));
            throw new CustomException(ErrorCode.FLIGHT_NOT_FOUND, "항공편을 찾을 수 없습니다: TravelFlight ID = " + travelFlightId);
        }
        TravelFlight travelFlight = travelFlightOpt.get();
        log.debug("조회된 TravelFlight 데이터: id={}, flightId={}, airlines={}, departureTime={}",
                travelFlight.getId(), travelFlight.getFlightId(), travelFlight.getAirlines(),
                travelFlight.getDepartureTime());
        FlightInfo flightInfo = buildFlightInfo(travelFlight);
        log.info("FlightInfo 생성 완료: flightId={}, travelFlightId={}", flightInfo.getId(), flightInfo.getTravelFlightId());
        return flightInfo;
    }

    private FlightInfo buildFlightInfo(TravelFlight travelFlight) {
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
        flightInfo.setPrice(travelFlight.getPrice() != null ? travelFlight.getPrice() : "250000");
        flightInfo.setCurrency(travelFlight.getCurrency() != null ? travelFlight.getCurrency() : "KRW");
        flightInfo.setCabinBaggage(travelFlight.getCabinBaggage() != null ? travelFlight.getCabinBaggage() : "Weight: 20kg");
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
        return flightInfo;
    }

    private Long saveFlightForSearch(FlightInfo flightInfo, FlightSearchReqDto reqDto) {
        log.info("항공편 저장 요청: flightId={}", flightInfo.getId());
        Optional<TravelPlan> travelPlanOpt = travelPlanRepository.findFirstByOrderByIdDesc();
        TravelPlan travelPlan = travelPlanOpt.orElseGet(() -> {
            log.warn("여행 계획 없음, 기본 여행 계획 생성");
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

        long startTime = System.currentTimeMillis();
        travelFlight = travelFlightRepository.save(travelFlight);
        log.info("항공편 저장 성공: id={}, flightId={}, 소요 시간: {}ms",
                travelFlight.getId(), travelFlight.getFlightId(), System.currentTimeMillis() - startTime);
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

        long startTime = System.currentTimeMillis();
        travelFlight = travelFlightRepository.save(travelFlight);
        log.info("항공편 저장 성공: id = {}, flightId = {}, 소요 시간: {}ms",
                travelFlight.getId(), travelFlight.getFlightId(), System.currentTimeMillis() - startTime);
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

    @Transactional
    public void saveBooking(Long accountId, AccountFlightRequestDto requestDto) {
        log.info("예약 저장 요청: accountId={}, flightId={}", accountId, requestDto.getFlightId());

        // accountId 검증
        if (accountId == null) {
            log.error("accountId가 null입니다. SecurityContext에서 사용자 정보를 확인합니다.");
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            log.info("Extracted email from SecurityContext: {}", email);
            Account accountByEmail = accountRepository.findByEmail(email)
                    .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER, "사용자를 찾을 수 없습니다: " + email));
            accountId = accountByEmail.getId();
            log.info("계정 조회 성공: accountId={}", accountId);
        }
        final Long finalAccountId = accountId;
        // Account 조회
        Account account = accountRepository.findById(finalAccountId)
                .orElseThrow(() -> {
                    log.error("사용자를 찾을 수 없음: accountId={}", finalAccountId);
                    return new CustomException(ErrorCode.NOT_FOUND_USER, "사용자를 찾을 수 없습니다: " + finalAccountId);
                });

        // 항공편 유효성 검사
        TravelFlight travelFlight = travelFlightRepository.findByFlightId(requestDto.getFlightId())
                .orElseThrow(() -> {
                    log.error("항공편을 찾을 수 없음: flightId={}", requestDto.getFlightId());
                    return new CustomException(ErrorCode.FLIGHT_NOT_FOUND, "항공편을 찾을 수 없습니다: " + requestDto.getFlightId());
                });

        // 예약 엔티티 생성
        AccountFlight accountFlight = AccountFlight.builder()
                .account(account)
                .flightId(requestDto.getFlightId())
                .carrier(requestDto.getCarrier())
                .carrierCode(requestDto.getCarrierCode())
                .flightNumber(requestDto.getFlightNumber())
                .departureAirport(requestDto.getDepartureAirport())
                .arrivalAirport(requestDto.getArrivalAirport())
                .departureTime(requestDto.getDepartureTime())
                .arrivalTime(requestDto.getArrivalTime())
                .returnDepartureAirport(requestDto.getReturnDepartureAirport())
                .returnArrivalAirport(requestDto.getReturnArrivalAirport())
                .returnDepartureTime(requestDto.getReturnDepartureTime())
                .returnArrivalTime(requestDto.getReturnArrivalTime())
                .passengerCount(requestDto.getPassengerCount())
                .selectedSeats(String.join(",", requestDto.getSelectedSeats()))
                .totalPrice(requestDto.getTotalPrice())
                .status("RESERVED")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // 탑승객 및 연락처 정보 로깅
        log.info("탑승객 정보: {}", requestDto.getPassengers());
        log.info("연락처 정보: {}", requestDto.getContact());

        // 예약 저장
        accountFlightRepository.save(accountFlight);
        log.info("예약 저장 완료: id={}, flightId={}", accountFlight.getId(), requestDto.getFlightId());
    }

    @Transactional(readOnly = true)
    public List<AccountFlightResponseDto> getUserBookings(Long accountId) {
        log.info("사용자 예약 조회 요청: accountId={}", accountId);

        List<AccountFlight> bookings = accountFlightRepository.findByAccountId(accountId);
        return bookings.stream().map(booking -> {
            AccountFlightResponseDto dto = new AccountFlightResponseDto();
            dto.setId(booking.getId());
            dto.setFlightId(booking.getFlightId());
            dto.setCarrier(booking.getCarrier());
            dto.setCarrierCode(booking.getCarrierCode());
            dto.setFlightNumber(booking.getFlightNumber());
            dto.setDepartureAirport(booking.getDepartureAirport());
            dto.setArrivalAirport(booking.getArrivalAirport());
            dto.setDepartureTime(booking.getDepartureTime());
            dto.setArrivalTime(booking.getArrivalTime());
            dto.setReturnDepartureAirport(booking.getReturnDepartureAirport());
            dto.setReturnArrivalAirport(booking.getReturnArrivalAirport());
            dto.setReturnDepartureTime(booking.getReturnDepartureTime());
            dto.setReturnArrivalTime(booking.getReturnArrivalTime());
            dto.setPassengerCount(booking.getPassengerCount());
            dto.setSelectedSeats(List.of(booking.getSelectedSeats().split(",")));
            dto.setTotalPrice(booking.getTotalPrice());
            dto.setStatus(booking.getStatus());
            dto.setCreatedAt(booking.getCreatedAt());
            return dto;
        }).collect(Collectors.toList());
    }
}


//    static class MockFlightData {
//        public static List<FlightInfo> getFlights(String origin, String destination, String departureDate, String returnDate) {
//            List<FlightInfo> flights = new ArrayList<>();
//            FlightInfo flight = new FlightInfo();
//            flight.setId(String.format("FL001-%s-%s-RT", origin, destination));
//            flight.setTravelFlightId(5L);
//            flight.setCarrier("대한항공");
//            flight.setCarrierCode("KE");
//            flight.setFlightNumber("KE123");
//            flight.setDepartureAirport(origin);
//            flight.setArrivalAirport(destination);
//            flight.setDepartureTime(LocalDateTime.parse(departureDate + "T10:00:00").toString());
//            flight.setArrivalTime(LocalDateTime.parse(departureDate + "T11:15:00").toString());
//            flight.setDepartureTimeZone(origin.equals("LHR") ? "Europe/London" : "Europe/Paris");
//            flight.setArrivalTimeZone(destination.equals("CDG") ? "Europe/Paris" : "Europe/London");
//            flight.setDuration("PT1H15M");
//            flight.setPrice("1200000");
//            flight.setCurrency("KRW");
//            flight.setCabinBaggage("Weight: 20kg");
//            flight.setNumberOfBookableSeats(50);
//            if (returnDate != null && !returnDate.isEmpty()) {
//                flight.setReturnDepartureTime(LocalDateTime.parse(returnDate + "T08:00:00").toString());
//                flight.setReturnArrivalTime(LocalDateTime.parse(returnDate + "T09:15:00").toString());
//                flight.setReturnDepartureTimeZone(destination.equals("CDG") ? "Europe/Paris" : "Europe/London");
//                flight.setReturnArrivalTimeZone(origin.equals("LHR") ? "Europe/London" : "Europe/Paris");
//                flight.setReturnDepartureAirport(destination);
//                flight.setReturnArrivalAirport(origin);
//                flight.setReturnDuration("PT1H15M");
//                flight.setReturnCarrier("대한항공");
//                flight.setReturnCarrierCode("KE");
//                flight.setReturnFlightNumber("KE124");
//            }
//            flights.add(flight);
//            log.info("MockFlightData: 생성된 항공편 - id={}, travelFlightId={}, origin={}, destination={}",
//                    flight.getId(), flight.getTravelFlightId(), origin, destination);
//            return flights;
//        }

