package com.backfunctionimpl.travel.travelPlace.service;

import com.backfunctionimpl.account.entity.Account;
import com.backfunctionimpl.travel.travelPlace.dto.SavedPlaceRequestDto;
import com.backfunctionimpl.travel.travelPlace.dto.SavedPlaceResponseDto;
import com.backfunctionimpl.travel.travelPlace.entity.SavedPlace;
import com.backfunctionimpl.travel.travelPlace.repository.SavedPlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SavedPlaceService {

    private final SavedPlaceRepository savedPlaceRepository;

    public SavedPlace save(SavedPlaceRequestDto dto, Account account) {
        SavedPlace place = new SavedPlace();
        place.setPlaceId(dto.getPlaceId());
        place.setName(dto.getName());
        place.setCity(dto.getCity());
        place.setCountry(dto.getCountry());
        place.setImage(dto.getImage());
        place.setType(dto.getType());
        place.setAccount(account);

        return savedPlaceRepository.save(place);
    }

    public List<SavedPlaceResponseDto> findAllByAccount(Account account) {
        return savedPlaceRepository.findAllByAccount(account).stream()
                .map(p -> new SavedPlaceResponseDto(
                        p.getId(), p.getName(), p.getType(), p.getCity(), p.getCountry(), p.getImage(), p.getSavedDate().toString()
                ))
                .collect(Collectors.toList());
    }
}