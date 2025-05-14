package com.backfunctionimpl.travel.travelPlace.controller;

import com.backfunctionimpl.travel.travelPlace.dto.SimplePlaceDto;
import com.backfunctionimpl.travel.travelPlace.service.GooglePlacesService;
import com.backfunctionimpl.travel.travelPlace.util.CountryCityMapper;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/places")
public class PlacesController {

    private final GooglePlacesService googlePlacesService;
    private final RestTemplate restTemplate;

    @Value("${google.maps-key}")
    private String apiKey;

    @GetMapping("/nearby")
    public ResponseEntity<?> getNearbyPlaces(
            @RequestParam(name = "lat", required = false) Double lat,
            @RequestParam(name = "lng", required = false) Double lng,
            @RequestParam(name = "city") String city,
            @RequestParam(name = "cityId") String cityId
    ) {
        if ("all".equals(cityId)) {
            List<SimplePlaceDto> total = new ArrayList<>();

            Map<String, double[]> cityMap = CountryCityMapper.getAllCities(); 

            for (Map.Entry<String, double[]> entry : cityMap.entrySet()) {
                String id = entry.getKey();
                double[] coords = entry.getValue();
                List<SimplePlaceDto> results = googlePlacesService.searchNearbyPlaces(
                        coords[0], coords[1], id, id
                );
                total.addAll(results);
            }

            return ResponseEntity.ok(total);
        }

        // 기존 처리
        List<SimplePlaceDto> result = googlePlacesService.searchNearbyPlaces(lat, lng, city, cityId);
        return ResponseEntity.ok(result);
    }


    @GetMapping("/search")
    public ResponseEntity<?> searchByKeyword(@RequestParam("keyword") String keyword) {
        keyword = keyword
                .replaceAll("[‘’“”'\"`´]", "")
                .replaceAll("\\p{C}", "")
                .replaceAll("\\p{Z}", "")
                .replaceAll("[^\\p{IsHangul}\\p{IsAlphabetic}]", "")
                .trim();

        Optional<double[]> coords = CountryCityMapper.getCoordinates(keyword);
        if (coords.isEmpty()) {
            Map<String, String> error = Map.of("error", "지정한 도시의 좌표를 찾을 수 없습니다.");
            return ResponseEntity.badRequest().body(error);
        }

        double[] latLng = coords.get();
        List<SimplePlaceDto> simplified = googlePlacesService.searchNearbyPlaces(
                latLng[0], latLng[1], keyword, keyword.toLowerCase()
        );

        return ResponseEntity.ok(simplified);
    }

    @GetMapping("/photo")
    public ResponseEntity<byte[]> getPhoto(@RequestParam("photo_reference") String photoReference) {
        try {
            String url = "https://maps.googleapis.com/maps/api/place/photo" +
                    "?maxwidth=400" +
                    "&photo_reference=" + photoReference +
                    "&key=" + apiKey;

            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(List.of(MediaType.IMAGE_JPEG));
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<byte[]> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, byte[].class
            );

            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(response.getBody());

        } catch (Exception e) {
            System.err.println("🛑 이미지 요청 실패: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }



    @GetMapping("/detail")
    public ResponseEntity<?> getPlaceDetail(@RequestParam("placeId") String placeId) {
        JsonNode detail = googlePlacesService.getPlaceDetail(placeId);
        return ResponseEntity.ok(detail);
    }



}
