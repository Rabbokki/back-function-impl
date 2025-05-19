package com.backfunctionimpl.travel.travelPlan.service;

import com.backfunctionimpl.account.entity.Account;
import com.backfunctionimpl.account.repository.AccountRepository;
import com.backfunctionimpl.travel.travelPlan.dto.TravelPlanRequest;
import com.backfunctionimpl.travel.travelPlan.dto.TravelPlanResponse;
import com.backfunctionimpl.travel.travelPlan.entity.TravelPlan;
import com.backfunctionimpl.travel.travelPlan.repository.TravelPlanRepository;
import com.backfunctionimpl.travel.travelAccommodation.entity.TravelAccommodation;
import com.backfunctionimpl.travel.travelPlace.entity.TravelPlace;
import com.backfunctionimpl.travel.travelTransportation.entity.TravelTransportation;
import com.backfunctionimpl.travel.travelTransportation.enums.Type;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class TravelPlanService {
    private static final Logger logger = LoggerFactory.getLogger(TravelPlanService.class);
    private final TravelPlanRepository travelPlanRepository;
    private final AccountRepository accountRepository;
    private final ObjectMapper objectMapper;

    public TravelPlanService(TravelPlanRepository travelPlanRepository, AccountRepository accountRepository, ObjectMapper objectMapper) {
        this.travelPlanRepository = travelPlanRepository;
        this.accountRepository = accountRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public TravelPlanResponse saveTravelPlan(TravelPlanRequest request, String userId) {
        try {
            logger.info("Received travel plan request: city={}, country={}, startDate={}, endDate={}, planType={}, places={}, travelPlanId={}",
                    request.getCity(), request.getCountry(), request.getStartDate(), request.getEndDate(), request.getPlanType(),
                    request.getPlaces() != null ? request.getPlaces().size() : 0, request.getTravelPlanId());

            // 입력 검증
            if (request.getCity() == null || request.getCity().trim().isEmpty()) {
                logger.error("도시명이 누락되었습니다.");
                throw new IllegalArgumentException("도시명이 누락되었습니다.");
            }

            // Account 조회
            Account account = accountRepository.findByEmail(userId)
                    .orElseThrow(() -> new RuntimeException("User not found: " + userId));

            TravelPlan travelPlan;

            // travelPlanId가 있으면 기존 일정 업데이트
            if (request.getTravelPlanId() != null) {
                travelPlan = travelPlanRepository.findById(request.getTravelPlanId())
                        .orElseThrow(() -> new RuntimeException("Travel plan not found: " + request.getTravelPlanId()));
                logger.info("Updating existing travel plan: id={}", request.getTravelPlanId());
            } else {
                // 중복 일정 확인
                LocalDate startDate = parseDate(request.getStartDate(), "start_date");
                LocalDate endDate = parseDate(request.getEndDate(), "end_date");
                List<TravelPlan> existingPlans = travelPlanRepository.findByAccountAndCityAndStartDateAndEndDateAndPlanType(
                        account, request.getCity(), startDate, endDate, request.getPlanType());
                if (!existingPlans.isEmpty()) {
                    logger.warn("Duplicate travel plan found for user: {}, city: {}, startDate: {}, endDate: {}, planType: {}",
                            userId, request.getCity(), startDate, endDate, request.getPlanType());
                    travelPlan = existingPlans.get(0); // 첫 번째 일정 사용
                } else {
                    travelPlan = TravelPlan.builder()
                            .city(request.getCity())
                            .country(request.getCountry())
                            .startDate(startDate)
                            .endDate(endDate)
                            .planType(request.getPlanType() != null ? request.getPlanType() : "MY")
                            .account(account)
                            .travelPlaces(new ArrayList<>())
                            .travelAccommodations(new ArrayList<>())
                            .travelTransportations(new ArrayList<>())
                            .build();
                }
            }

            // TravelPlace 리스트 생성
            List<TravelPlace> places = request.getPlaces() != null
                    ? request.getPlaces().stream()
                    .map(dto -> {
                        TravelPlace place = new TravelPlace();
                        place.setName(dto.getName());
                        place.setAddress(dto.getAddress());
                        place.setDay(dto.getDay());
                        place.setCategory(dto.getCategory());
                        place.setDescription(dto.getDescription());
                        place.setLat(dto.getLatitude() != null ? dto.getLatitude() : 0.0);
                        place.setLng(dto.getLongitude() != null ? dto.getLongitude() : 0.0);
                        try {
                            if (dto.getTime() != null && !dto.getTime().isEmpty()) {
                                place.setTime(LocalTime.parse(dto.getTime()));
                            }
                        } catch (DateTimeParseException e) {
                            logger.warn("Invalid time format: {}. Skipping time.", dto.getTime());
                        }
                        place.setTravelPlan(travelPlan);
                        return place;
                    })
                    .collect(Collectors.toList())
                    : new ArrayList<>();

            // TravelAccommodation 리스트 생성
            List<TravelAccommodation> accommodations = request.getAccommodations() != null
                    ? request.getAccommodations().stream()
                    .map(dto -> {
                        TravelAccommodation acc = new TravelAccommodation();
                        acc.setName(dto.getName());
                        acc.setAddress(dto.getAddress());
                        acc.setDay(dto.getDay());
                        acc.setDescription(dto.getDescription());
                        acc.setLatitude(dto.getLatitude() != null ? dto.getLatitude() : 0.0);
                        acc.setLongitude(dto.getLongitude() != null ? dto.getLongitude() : 0.0);
                        try {
                            if (dto.getCheckInDate() != null && !dto.getCheckInDate().isEmpty()) {
                                acc.setCheckInDate(LocalDateTime.parse(dto.getCheckInDate()));
                            }
                            if (dto.getCheckOutDate() != null && !dto.getCheckOutDate().isEmpty()) {
                                acc.setCheckOutDate(LocalDateTime.parse(dto.getCheckOutDate()));
                            }
                        } catch (DateTimeParseException e) {
                            logger.warn("Invalid date format for accommodation: {}. Skipping dates.", e.getMessage());
                        }
                        acc.setTravelPlan(travelPlan);
                        return acc;
                    })
                    .collect(Collectors.toList())
                    : new ArrayList<>();

            // TravelTransportation 리스트 생성
            List<TravelTransportation> transportations = request.getTransportations() != null
                    ? request.getTransportations().stream()
                    .map(dto -> {
                        TravelTransportation trans = new TravelTransportation();
                        try {
                            if (dto.getType() != null && !dto.getType().isEmpty()) {
                                trans.setType(Type.valueOf(dto.getType()));
                            } else {
                                logger.warn("Transportation type is null or empty. Skipping.");
                                return null;
                            }
                        } catch (IllegalArgumentException e) {
                            logger.warn("Invalid transportation type: {}. Skipping.", dto.getType());
                            return null;
                        }
                        trans.setDay(dto.getDay());
                        trans.setTravelPlan(travelPlan);
                        return trans;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList())
                    : new ArrayList<>();

            // TravelPlan에 리스트 설정
            travelPlan.setTravelPlaces(places);
            travelPlan.setTravelAccommodations(accommodations);
            travelPlan.setTravelTransportations(transportations);

            // TravelPlan 저장
            TravelPlan savedPlan = travelPlanRepository.save(travelPlan);
            logger.info("Travel plan saved successfully for user: {}, city: {}, planType: {}", userId, savedPlan.getCity(), savedPlan.getPlanType());

            // TravelPlanResponse 생성
            return TravelPlanResponse.builder()
                    .id(savedPlan.getId())
                    .destination(savedPlan.getCity())
                    .startDate(savedPlan.getStartDate().format(DateTimeFormatter.ISO_LOCAL_DATE))
                    .endDate(savedPlan.getEndDate().format(DateTimeFormatter.ISO_LOCAL_DATE))
                    .planType(savedPlan.getPlanType())
                    .status("예정")
                    .build();
        } catch (Exception e) {
            logger.error("Error saving travel plan: {}", e.getMessage(), e);
            throw new RuntimeException("여행 계획 저장 중 오류 발생: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<TravelPlanResponse> getPlansByUserId(String userId) {
        try {
            List<TravelPlan> plans = travelPlanRepository.findByAccountEmail(userId);
            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
            LocalDate now = LocalDate.now();

            return plans.stream().map(plan -> {
                TravelPlanResponse response = TravelPlanResponse.builder()
                        .id(plan.getId())
                        .destination(plan.getCity())
                        .startDate(plan.getStartDate().format(formatter))
                        .endDate(plan.getEndDate().format(formatter))
                        .planType(plan.getPlanType())
                        .build();
                try {
                    LocalDate endDate = LocalDate.parse(response.getEndDate(), formatter);
                    response.setStatus(now.isAfter(endDate) ? "완료" : "예정");
                } catch (DateTimeParseException e) {
                    logger.warn("Invalid end_date format: {}. Setting status to '예정'.", response.getEndDate());
                    response.setStatus("예정");
                }
                return response;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching travel plans: {}", e.getMessage(), e);
            throw new RuntimeException("여행 계획 조회 중 오류: " + e.getMessage());
        }
    }

    private LocalDate parseDate(String dateStr, String fieldName) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            logger.warn("Missing {}: {}. Using current date.", fieldName, dateStr);
            return LocalDate.now();
        }
        try {
            return LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
            logger.warn("Invalid {} format: {}. Using current date.", fieldName, dateStr);
            return LocalDate.now();
        }
    }
}


