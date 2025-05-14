package com.backfunctionimpl.travel.travelPlan.controller;

import com.backfunctionimpl.global.security.user.UserDetailsImpl;
import com.backfunctionimpl.travel.travelPlan.dto.TravelPlanSaveRequestDto;
import com.backfunctionimpl.travel.travelPlan.service.TravelPlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // 로그 출력을 위한 어노테이션 추가
import org.springframework.http.HttpStatus; // 에러 응답용
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/travel-plans")
@RequiredArgsConstructor
@Slf4j
public class TravelPlanController {

    private final TravelPlanService travelPlanService;

    @PostMapping
    public ResponseEntity<?> saveTravelPlan(@RequestBody TravelPlanSaveRequestDto dto,
                                            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            // ✅ [수정 4] 유저 정보 null 확인
            if (userDetails == null || userDetails.getAccount() == null) {
                log.warn("❌ 사용자 인증 정보 없음: userDetails is null");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
            }

            log.info("✅ 여행 저장 요청: accountId={}, city={}, date={}~{}",
                    userDetails.getAccount().getId(), dto.getCity(), dto.getStartDate(), dto.getEndDate());

            travelPlanService.save(dto, userDetails.getAccount());

            log.info("✅ 여행 저장 성공");
            return ResponseEntity.ok("여행 일정 저장 완료!");

        } catch (Exception e) {
            // ✅ [수정 5] 예외 발생 시 로그와 500 응답
            log.error("❌ 여행 저장 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("여행 저장 중 오류 발생: " + e.getMessage());
        }
    }
}
