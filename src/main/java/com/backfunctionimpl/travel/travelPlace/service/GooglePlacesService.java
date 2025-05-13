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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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

        // 1. 요청 바디 (JSON)
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

        // 2. 요청 헤더
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Goog-Api-Key", apiKey);
        headers.set("X-Goog-FieldMask", "places.displayName,places.formattedAddress,places.location,places.photos");

        // 3. 요청 전송
        HttpEntity<String> request = new HttpEntity<>(body, headers);
        ResponseEntity<JsonNode> response = restTemplate.exchange(
                url, HttpMethod.POST, request, JsonNode.class
        );

        // 4. 응답 파싱
        JsonNode places = response.getBody().get("places");
        List<SimplePlaceDto> results = new ArrayList<>();

        if (places != null && places.isArray()) {
            for (JsonNode place : places) {
                String name = place.path("displayName").path("text").asText(null);
                String address = place.path("formattedAddress").asText(null);
                double rating = 0.0;

                String photoName = place.path("photos").isArray() && place.path("photos").size() > 0
                        ? place.path("photos").get(0).path("name").asText(null)
                        : null;

                String imageUrl;
                if (photoName != null && photoName.startsWith("places/")) {
                    imageUrl = String.format("places/%s", photoName.split("/")[photoName.split("/").length - 1]);
                } else {
                    imageUrl = photoName;
                }


                results.add(new SimplePlaceDto(
                        name,
                        name,
                        address,
                        rating,
                        imageUrl,
                        "관광지",
                        city,
                        cityId,
                        (int)(Math.random() * 1000)
                ));
            }
        }


        return results;
    }
}
