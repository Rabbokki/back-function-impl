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
        String url = String.format(
                "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=%f,%f&radius=4000&type=tourist_attraction&language=ko&key=%s",
                lat, lng, apiKey
        );

        ResponseEntity<JsonNode> response = restTemplate.getForEntity(url, JsonNode.class);

        JsonNode results = response.getBody().path("results");
        System.out.println("✅ 구글 Nearby 결과: " + results);

        List<SimplePlaceDto> places = new ArrayList<>();

        if (results != null && results.isArray()) {
            for (JsonNode node : results) {
                String name = node.path("name").asText(null);
                String address = node.path("vicinity").asText(null);
                double rating = node.path("rating").asDouble(0);
                int reviewCount = node.path("user_ratings_total").asInt(0); // ✅ 리뷰 수
                String placeId = node.path("place_id").asText(null);

                String photoRef = null;
                if (node.has("photos") && node.get("photos").isArray() && node.get("photos").size() > 0) {
                    photoRef = node.get("photos").get(0).path("photo_reference").asText(null);
                }

                String imageUrl = (photoRef != null)
                        ? String.format("https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photo_reference=%s&key=%s", photoRef, apiKey)
                        : null;

                // ✅ reviewCount 반영
                places.add(new SimplePlaceDto(
                        name,
                        name,
                        address,
                        rating,
                        imageUrl,
                        "관광지",
                        city,
                        cityId,
                        reviewCount,
                        placeId
                ));
            }
            places.sort((a, b) -> Integer.compare(b.getReviewCount(), a.getReviewCount()));
        }

        return places;
    }



    public JsonNode getPlaceDetail(String placeId) {

        System.out.println("📍 상세조회 요청된 placeId: " + placeId);
        String url = String.format(
                "https://maps.googleapis.com/maps/api/place/details/json?place_id=%s&fields=name,formatted_address,formatted_phone_number,international_phone_number,editorial_summary,opening_hours,website,rating,user_ratings_total,photos,reviews&language=ko&key=%s",
                placeId,
                apiKey
        );

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.GET, request, JsonNode.class);

        return response.getBody().path("result"); // "result" 노드에 상세정보 있음
    }

}
