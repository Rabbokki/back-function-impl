//package com.backfunctionimpl.travel.travelPlan.controller;
//
//import com.backfunctionimpl.travel.travelPlan.dto.TravelPlanSaveRequestDto;
//import com.backfunctionimpl.travel.travelPlan.service.TravelPlanService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/travel-plans")
//@RequiredArgsConstructor
//public class TravelPlanController {
//
//    private final TravelPlanService travelPlanService;
//
//    @PostMapping
//    public ResponseEntity<?> saveTravelPlan(@RequestBody TravelPlanSaveRequestDto dto) {
//        travelPlanService.save(dto);
//        return ResponseEntity.ok("여행 일정 저장 완료!");
//    }
//}
