package com.backfunctionimpl.travel.travelPlace.controller;

import com.backfunctionimpl.account.entity.Account;
import com.backfunctionimpl.account.repository.AccountRepository;
import com.backfunctionimpl.travel.travelPlace.dto.SavedPlaceRequestDto;
import com.backfunctionimpl.travel.travelPlace.dto.SavedPlaceResponseDto;
import com.backfunctionimpl.travel.travelPlace.entity.SavedPlace;
import com.backfunctionimpl.travel.travelPlace.repository.SavedPlaceRepository;
import com.backfunctionimpl.travel.travelPlace.service.SavedPlaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/saved-places")
public class SavedPlaceController {

    private final SavedPlaceService savedPlaceService;
    private final AccountRepository accountRepository;
    private final SavedPlaceRepository savedPlaceRepository;

    @PostMapping
    public ResponseEntity<?> savePlace(@RequestBody SavedPlaceRequestDto dto,
                                       @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        Account account = accountRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

        savedPlaceService.save(dto, account);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "저장 완료");
        response.put("data", null); // ← Map.of() 말고 HashMap 사용!

        return ResponseEntity.ok(response);
    }



    @GetMapping
    public ResponseEntity<?> getSavedPlaces(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (userDetails == null) {
                Map<String, Object> errorRes = new HashMap<>();
                errorRes.put("success", false);
                errorRes.put("message", "로그인 정보가 없습니다.");
                errorRes.put("data", null);
                return ResponseEntity.status(401).body(errorRes);
            }

            Account account = accountRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

            List<SavedPlaceResponseDto> places = savedPlaceService.getSavedPlaces(account);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", places); // ✅ null 이더라도 안전
            result.put("message", "조회 성공");
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("❌ 예외 발생: {}", e.getMessage(), e);

            Map<String, Object> errorRes = new HashMap<>();
            errorRes.put("success", false);
            errorRes.put("data", null);
            errorRes.put("error", e.getMessage() != null ? e.getMessage() : "서버 오류 발생");

            return ResponseEntity.status(500).body(errorRes);
        }
    }

    @DeleteMapping("/{placeId}")
    public ResponseEntity<?> deleteSavedPlace(@PathVariable("placeId") String placeId,
                                              @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            Map<String, Object> errorRes = new HashMap<>();
            errorRes.put("success", false);
            errorRes.put("message", "로그인이 필요합니다.");
            errorRes.put("data", null);
            return ResponseEntity.status(401).body(errorRes);
        }

        Account account = accountRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

        savedPlaceService.deleteByPlaceIdAndAccount(placeId, account);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "삭제 성공");
        result.put("data", null);

        return ResponseEntity.ok(result);
    }



}
