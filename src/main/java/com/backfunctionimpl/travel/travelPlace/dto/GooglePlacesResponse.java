package com.backfunctionimpl.travel.travelPlace.dto;

import com.backfunctionimpl.travel.travelPlace.dto.PlaceResult;
import lombok.Data;
import java.util.List;

@Data
public class GooglePlacesResponse {
    private List<PlaceResult> results;
    private String status;
}
