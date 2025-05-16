package com.backfunctionimpl.travel.travelPlan.service;

import com.backfunctionimpl.account.entity.Account;
import com.backfunctionimpl.account.repository.AccountRepository;
import com.backfunctionimpl.travel.travelPlan.dto.TravelPlanResponseDto;
import com.backfunctionimpl.travel.travelPlan.dto.TravelPlanSaveRequestDto;
import com.backfunctionimpl.travel.travelPlan.entity.TravelPlan;
import com.backfunctionimpl.travel.travelPlan.repository.TravelPlanRepository;
import com.backfunctionimpl.travel.travelAccommodation.entity.TravelAccommodation;
import com.backfunctionimpl.travel.travelAccommodation.repository.TravelAccommodationRepository;
import com.backfunctionimpl.travel.travelPlace.entity.TravelPlace;
import com.backfunctionimpl.travel.travelPlace.repository.TravelPlaceRepository;
import com.backfunctionimpl.travel.travelTransportation.entity.TravelTransportation;
import com.backfunctionimpl.travel.travelTransportation.repository.TravelTransportationRepository;
import com.backfunctionimpl.travel.travelTransportation.enums.Type;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
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
    public void saveTravelPlan(String userId, Map<String, Object> planData) {
        try {
            // 입력 검증
            String city = (String) planData.get("city");
            if (city == null || city.trim().isEmpty()) {
                throw new IllegalArgumentException("도시명이 누락되었습니다.");
            }

            TravelPlan travelPlan = new TravelPlan();
            travelPlan.setCity(city);
            travelPlan.setCountry((String) planData.get("country"));
            travelPlan.setPlanType("MY");

            DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE;
            try {
                travelPlan.setStartDate(LocalDate.parse((String) planData.get("start_date"), dateFormatter));
            } catch (DateTimeParseException e) {
                logger.warn("Invalid start_date format: {}. Using current date.", planData.get("start_date"));
                travelPlan.setStartDate(LocalDate.now());
            }
            try {
                travelPlan.setEndDate(LocalDate.parse((String) planData.get("end_date"), dateFormatter));
            } catch (DateTimeParseException e) {
                logger.warn("Invalid end_date format: {}. Using start_date or current date.", planData.get("end_date"));
                travelPlan.setEndDate(travelPlan.getStartDate() != null ? travelPlan.getStartDate() : LocalDate.now());
            }

            Account account = accountRepository.findByEmail(userId)
                    .orElseThrow(() -> new RuntimeException("User not found: " + userId));
            travelPlan.setAccount(account);

            // 장소 처리
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> places = (List<Map<String, Object>>) planData.get("places");
            if (places != null) {
                for (Map<String, Object> placeData : places) {
                    TravelPlace place = new TravelPlace();
                    place.setName((String) placeData.get("name"));
                    place.setAddress((String) placeData.get("address"));
                    place.setDay((String) placeData.get("day"));
                    place.setCategory((String) placeData.get("category"));
                    place.setDescription((String) placeData.get("description"));
                    try {
                        String timeStr = (String) placeData.get("time");
                        if (timeStr != null && !timeStr.isEmpty()) {
                            place.setTime(LocalTime.parse(timeStr));
                        }
                    } catch (DateTimeParseException e) {
                        logger.warn("Invalid time format: {}. Skipping time.", placeData.get("time"));
                    }
                    place.setLat(parseDoubleOrDefault(placeData.get("latitude"), 0.0));
                    place.setLng(parseDoubleOrDefault(placeData.get("longitude"), 0.0));
                    place.setTravelPlan(travelPlan);
                    travelPlan.getTravelPlaces().add(place);
                }
            }

            // 숙소 처리
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> accommodations = (List<Map<String, Object>>) planData.get("accommodations");
            if (accommodations != null) {
                for (Map<String, Object> accData : accommodations) {
                    TravelAccommodation acc = new TravelAccommodation();
                    acc.setName((String) accData.get("name"));
                    acc.setAddress((String) accData.get("address"));
                    acc.setDay((String) accData.get("day"));
                    acc.setDescription((String) accData.get("description"));
                    try {
                        String checkIn = (String) accData.get("check_in_date");
                        String checkOut = (String) accData.get("check_out_date");
                        if (checkIn != null && !checkIn.isEmpty()) {
                            acc.setCheckInDate(LocalDateTime.parse(checkIn));
                        }
                        if (checkOut != null && !checkOut.isEmpty()) {
                            acc.setCheckOutDate(LocalDateTime.parse(checkOut));
                        }
                    } catch (DateTimeParseException e) {
                        logger.warn("Invalid date format for accommodation: {}. Skipping dates.", e.getMessage());
                    }
                    acc.setLatitude(parseDoubleOrDefault(accData.get("latitude"), 0.0));
                    acc.setLongitude(parseDoubleOrDefault(accData.get("longitude"), 0.0));
                    acc.setTravelPlan(travelPlan);
                    travelPlan.getTravelAccommodations().add(acc);
                }
            }

            // 이동 수단 처리
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> transportations = (List<Map<String, Object>>) planData.get("transportations");
            if (transportations != null) {
                for (Map<String, Object> transData : transportations) {
                    TravelTransportation trans = new TravelTransportation();
                    try {
                        String typeStr = (String) transData.get("type");
                        if (typeStr != null && !typeStr.isEmpty()) {
                            trans.setType(Type.valueOf(typeStr));
                        } else {
                            logger.warn("Transportation type is null or empty. Skipping.");
                            continue;
                        }
                    } catch (IllegalArgumentException e) {
                        logger.warn("Invalid transportation type: {}. Skipping.", transData.get("type"));
                        continue;
                    }
                    trans.setDay((String) transData.get("day"));
                    trans.setTravelPlan(travelPlan);
                    travelPlan.getTravelTransportations().add(trans);
                }
            }

            travelPlanRepository.save(travelPlan);
            logger.info("Travel plan saved successfully for user: {}, city: {}, planType: MY", userId, travelPlan.getCity());
        } catch (Exception e) {
            logger.error("Error saving travel plan: {}", e.getMessage(), e);
            throw new RuntimeException("여행 계획 저장 중 오류 발생: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<TravelPlanResponseDto> findTravelPlansByUserId(String userId) {
        List<TravelPlan> plans = travelPlanRepository.findByAccountEmail(userId);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate now = LocalDate.now();

        return plans.stream().map(plan -> {
            TravelPlanResponseDto dto = new TravelPlanResponseDto();
            dto.setId(plan.getId());
            dto.setDestination(plan.getCity());
            dto.setStartDate(plan.getStartDate() != null ? plan.getStartDate().format(formatter) : now.format(formatter));
            dto.setEndDate(plan.getEndDate() != null ? plan.getEndDate().format(formatter) : now.format(formatter));
            try {
                LocalDate endLocalDate = LocalDate.parse(dto.getEndDate(), formatter);
                dto.setStatus(now.isAfter(endLocalDate) ? "완료" : "예정");
            } catch (Exception e) {
                dto.setStatus("예정");
            }
            dto.setPlanType(plan.getPlanType() != null ? plan.getPlanType() : "MY");
            return dto;
        }).collect(Collectors.toList());
    }

    private double parseDoubleOrDefault(Object value, double defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException e) {
            logger.warn("Invalid number format for value: {}. Using default: {}", value, defaultValue);
            return defaultValue;
        }
    }
}

