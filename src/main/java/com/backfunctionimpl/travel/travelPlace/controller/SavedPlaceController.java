package com.backfunctionimpl.travel.travelPlace.controller;

import com.backfunctionimpl.account.entity.Account;
import com.backfunctionimpl.travel.travelPlace.dto.SavedPlaceRequestDto;
import com.backfunctionimpl.travel.travelPlace.dto.SavedPlaceResponseDto;
import com.backfunctionimpl.travel.travelPlace.entity.SavedPlace;
import com.backfunctionimpl.travel.travelPlace.service.SavedPlaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/saved-places")
public class SavedPlaceController {

    private final SavedPlaceService savedPlaceService;

    @PostMapping
    public ResponseEntity<?> save(@RequestBody SavedPlaceRequestDto dto,
                                  @AuthenticationPrincipal Account account) {
        SavedPlace saved = savedPlaceService.save(dto, account);
        return ResponseEntity.ok("저장 완료");
    }

    @GetMapping
    public ResponseEntity<?> getSavedPlaces(@AuthenticationPrincipal Account account) {
        List<SavedPlaceResponseDto> savedPlaces = savedPlaceService.findAllByAccount(account);
        return ResponseEntity.ok(savedPlaces);
    }
}
