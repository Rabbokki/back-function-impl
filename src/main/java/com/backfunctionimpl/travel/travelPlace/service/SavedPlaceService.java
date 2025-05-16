package com.backfunctionimpl.travel.travelPlace.service;

import com.backfunctionimpl.account.entity.Account;
import com.backfunctionimpl.travel.travelPlace.dto.SavedPlaceRequestDto;
import com.backfunctionimpl.travel.travelPlace.dto.SavedPlaceResponseDto;
import com.backfunctionimpl.travel.travelPlace.entity.SavedPlace;
import com.backfunctionimpl.travel.travelPlace.repository.SavedPlaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import com.backfunctionimpl.travel.travelPlace.util.CountryCityMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class SavedPlaceService {

    private final SavedPlaceRepository savedPlaceRepository;

    public SavedPlace save(SavedPlaceRequestDto dto, Account account) {
        // 기본값 설정
        String name = dto.getName() != null ? dto.getName() : "이름없음";
        String city = dto.getCity() != null ? dto.getCity() : "도시없음";

        // ✅ city 기반 국가 자동 매핑
        String country = CountryCityMapper.getCountry(city);

        String type = dto.getType() != null ? dto.getType() : "명소";

        log.info("📝 저장 요청 - name: {}, city: {}, country: {}, type: {}", name, city, country, type);

        SavedPlace place = new SavedPlace();
        place.setPlaceId(dto.getPlaceId());
        place.setName(name);
        place.setCity(city);
        place.setCountry(country);
        place.setImage(dto.getImage());
        place.setType(type);
        place.setAccount(account);

        return savedPlaceRepository.save(place);
    }


    public List<SavedPlaceResponseDto> getSavedPlaces(Account account) {
        log.info("🛠 [service] 저장된 장소 조회 시작 for account id={}", account.getId());

        List<SavedPlace> savedList = savedPlaceRepository.findAllByAccountId(account.getId());

        log.info("🧾 [service] 조회된 entity 개수: {}", savedList.size());

        List<SavedPlaceResponseDto> dtoList = savedList.stream()
                .filter(p -> {
                    boolean valid = p != null && p.getName() != null && p.getPlaceId() != null;
                    if (!valid) log.warn("⚠️ 잘못된 엔티티 발견: {}", p);
                    return valid;
                }) // ✅ null 및 이상 데이터 제거
                .map(p -> {
                    log.info("➡️ mapping: placeId={}, name={}", p.getPlaceId(), p.getName());
                    return new SavedPlaceResponseDto(
                            p.getPlaceId(), p.getName(), p.getCity(), p.getCountry(),
                            p.getImage(), p.getType(), p.getCreatedAt());
                }).collect(Collectors.toList());

        return dtoList;
    }


    public void deleteByPlaceIdAndAccount(String placeId, Account account) {
        SavedPlace place = savedPlaceRepository.findByPlaceIdAndAccount(placeId, account)
                .orElseThrow(() -> new IllegalArgumentException("❌ 저장된 장소를 찾을 수 없습니다."));
        savedPlaceRepository.delete(place);
    }



}


