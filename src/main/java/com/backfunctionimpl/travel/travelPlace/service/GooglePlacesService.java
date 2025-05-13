package com.backfunctionimpl.travel.travelPlace.service;

import com.backfunctionimpl.travel.travelPlace.dto.SimplePlaceDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class GooglePlacesService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${google.maps-key}")
    private String apiKey;

    @PostConstruct
    public void debug() {
        System.out.println("✅ GOOGLE_MAPS_KEY = " + apiKey);
    }

    public List<SimplePlaceDto> searchNearbyPlaces(double lat, double lng, String city, String cityId) {
        String url = "https://places.googleapis.com/v1/places:searchNearby";

        String body = String.format("""
        {
          "includedTypes": ["tourist_attraction"],
          "locationRestriction": {
            "circle": {
              "center": {
                "latitude": %f,
                "longitude": %f
              },
              "radius": 3000.0
            }
          }
        }
        """, lat, lng);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Goog-Api-Key", apiKey);
        headers.set("X-Goog-FieldMask", "places.id,places.displayName,places.formattedAddress,places.photos");

        headers.set("Accept-Language", "ko");

        HttpEntity<String> request = new HttpEntity<>(body, headers);
        ResponseEntity<JsonNode> response = restTemplate.exchange(
                url, HttpMethod.POST, request, JsonNode.class
        );

        JsonNode places = response.getBody().get("places");
        List<SimplePlaceDto> results = new ArrayList<>();

        if (places != null && places.isArray()) {
            for (JsonNode place : places) {
                String name = place.path("displayName").path("text").asText(null);
                String address = place.path("formattedAddress").asText(null);
                double rating = 0.0;

                // ✅ placeId 추출
                String placeId = place.path("id").asText(null);

                // ✅ photoReference → image URL 생성
                String photoReference = null;
                if (place.has("photos") && place.get("photos").isArray() && place.get("photos").size() > 0) {
                    JsonNode photo = place.get("photos").get(0);
                    String photoName = photo.path("name").asText(null);
                    if (photoName != null && photoName.contains("/")) {
                        photoReference = photoName.substring(photoName.lastIndexOf("/") + 1);
                    }
                }

                String imageUrl = (photoReference != null)
                        ? String.format("https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photo_reference=%s&key=%s", photoReference, apiKey)
                        : null;

                // ✅ 최종 DTO에 placeId 포함!
                results.add(new SimplePlaceDto(
                        name,
                        name,
                        address,
                        rating,
                        imageUrl,
                        "관광지",
                        city,
                        cityId,
                        (int)(Math.random() * 1000),
                        placeId // ← 여기에 반드시 있어야 함!
                ));
            }

        }

        return results;
    }

    public JsonNode getPlaceDetail(String placeId) {

        System.out.println("📍 상세조회 요청된 placeId: " + placeId);
        String url = String.format(
                "https://maps.googleapis.com/maps/api/place/details/json?place_id=%s&fields=name,formatted_address,international_phone_number,opening_hours,website,rating,user_ratings_total,photos,reviews&language=ko&key=%s",
                placeId,
                apiKey
        );

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.GET, request, JsonNode.class);

        return response.getBody().path("result"); // "result" 노드에 상세정보 있음
    }

}
