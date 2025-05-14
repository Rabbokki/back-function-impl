//package com.backfunctionimpl.travel.travelPlan.service;
//
//import com.backfunctionimpl.account.entity.Account;
//import com.backfunctionimpl.account.repository.AccountRepository;
//import com.backfunctionimpl.travel.travelPlan.dto.TravelPlanSaveRequestDto;
//import com.backfunctionimpl.travel.travelPlan.entity.TravelPlan;
//import com.backfunctionimpl.travel.travelPlan.repository.TravelPlanRepository;
//import com.backfunctionimpl.travel.travelAccommodation.entity.TravelAccommodation;
//import com.backfunctionimpl.travel.travelAccommodation.repository.TravelAccommodationRepository;
//import com.backfunctionimpl.travel.travelPlace.entity.TravelPlace;
//import com.backfunctionimpl.travel.travelPlace.repository.TravelPlaceRepository;
//import com.backfunctionimpl.travel.travelTransportation.entity.TravelTransportation;
//import com.backfunctionimpl.travel.travelTransportation.repository.TravelTransportationRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import java.time.LocalTime;
//
//@Service
//@RequiredArgsConstructor
//public class TravelPlanService {
//
//    private final TravelPlanRepository travelPlanRepository;
//    private final TravelPlaceRepository travelPlaceRepository;
//    private final TravelAccommodationRepository travelAccommodationRepository;
//    private final TravelTransportationRepository travelTransportationRepository;
//    private final AccountRepository accountRepository;
//
//    public void save(TravelPlanSaveRequestDto dto) {
//        Account account = accountRepository.findById(dto.getAccountId())
//                .orElseThrow(() -> new IllegalArgumentException("계정 없음"));
//
//        TravelPlan plan = new TravelPlan();
//        plan.setStartDate(dto.getStartDate());
//        plan.setEndDate(dto.getEndDate());
//        plan.setCountry(dto.getCountry());
//        plan.setCity(dto.getCity());
//        plan.setAccount(account);
//
//        travelPlanRepository.save(plan);
//
//        // 장소 저장
//        dto.getPlaces().forEach(placeDto -> {
//            TravelPlace place = new TravelPlace();
//            place.setDay(placeDto.getDay());
//            place.setName(placeDto.getName());
//            place.setCategory(placeDto.getCategory());
//            place.setDescription(placeDto.getDescription());
//            place.setTime(LocalTime.parse(placeDto.getTime()));
//            place.setLat(placeDto.getLat());
//            place.setLng(placeDto.getLng());
//            place.setTravelPlan(plan);
//            travelPlaceRepository.save(place);
//        });
//
//        // 숙소 저장
//        dto.getAccommodations().forEach(accDto -> {
//            TravelAccommodation acc = new TravelAccommodation();
//            acc.setDay(accDto.getDay());
//            acc.setName(accDto.getName());
//            acc.setDescription(accDto.getDescription());
//            acc.setLat(accDto.getLat());
//            acc.setLng(accDto.getLng());
//            acc.setTravelPlan(plan);
//            travelAccommodationRepository.save(acc);
//        });
//
//        // 교통수단 저장
//        TravelTransportation transportation = new TravelTransportation();
//        transportation.setType(dto.getTransportation());
//        transportation.setTravelPlan(plan);
//        travelTransportationRepository.save(transportation);
//    }
//}
