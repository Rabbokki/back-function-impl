package com.backfunctionimpl.travel.travelPlan.service;

import com.backfunctionimpl.account.entity.Account;
import com.backfunctionimpl.account.repository.AccountRepository;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional

public class TravelPlanService {

    private final TravelPlanRepository travelPlanRepository;
    private final AccountRepository accountRepository;

    public void save(TravelPlanSaveRequestDto dto, Account account) {
        Account findAccount = accountRepository.findById(account.getId())
                .orElseThrow(() -> new IllegalArgumentException("계정 없음"));

        TravelPlan plan = new TravelPlan();
        plan.setStartDate(dto.getStartDate());
        plan.setEndDate(dto.getEndDate());
        plan.setCountry(dto.getCountry());
        plan.setCity(dto.getCity());
        plan.setAccount(findAccount);

        // 장소 저장 (연관관계만 연결, 저장은 plan 저장 시 자동)
        dto.getPlaces().forEach(placeDto -> {
            TravelPlace place = new TravelPlace();
            place.setDay(placeDto.getDay());
            place.setName(placeDto.getName());
            place.setCategory(placeDto.getCategory());
            place.setDescription(placeDto.getDescription());
            place.setTime(LocalTime.parse(placeDto.getTime()));
            place.setLat(placeDto.getLat());
            place.setLng(placeDto.getLng());
            place.setTravelPlan(plan);
            plan.getTravelPlaces().add(place); // 핵심
        });

        // 숙소 저장
        dto.getAccommodations().forEach(accDto -> {
            TravelAccommodation acc = new TravelAccommodation();
            acc.setDay(accDto.getDay());
            acc.setName(accDto.getName());
            acc.setDescription(accDto.getDescription());
            acc.setLat(accDto.getLat());
            acc.setLng(accDto.getLng());
            acc.setTravelPlan(plan);
            plan.getTravelAccommodations().add(acc);
        });

        // 교통수단 저장 (하나만 저장하므로 리스트로 처리하지 않음)
        TravelTransportation transportation = new TravelTransportation();
        transportation.setType(Type.valueOf(dto.getTransportation()));
        transportation.setTravelPlan(plan);
        plan.getTravelTransportations().add(transportation);


        // ✅ 로그 먼저 찍고
        log.info("💾 저장 시도: Account={}, 도시={}, 날짜={}", account.getEmail(), dto.getCity(), dto.getStartDate());
        log.info("💾 장소 개수={}, 숙소 개수={}, 교통수단={}", dto.getPlaces().size(), dto.getAccommodations().size(), dto.getTransportation());

        // ✅ try-catch 추가해서 실제로 실패했는지 확인
        try {
            travelPlanRepository.save(plan);
            log.info("✅ 여행 저장 성공");
        } catch (Exception e) {
            log.error("❌ 여행 저장 실패: {}", e.getMessage(), e);
            throw e; // 또는 커스텀 예외 던져도 됨
        }
    }
}

