package com.backfunctionimpl.travel.config;

import com.amadeus.Amadeus;
import com.amadeus.Params;
import com.amadeus.exceptions.NetworkException;
import com.amadeus.exceptions.ResponseException;
import com.amadeus.resources.Location;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class AmadeusClient {
    private final String clientId;
    private final String clientSecret;
    private String accessToken;
    private long tokenExpiryTime;
    private final WebClient webClient;

    private static final Map<String, String> CITY_TO_ENGLISH = new HashMap<>();

    static {
        CITY_TO_ENGLISH.put("서울", "Seoul");
        CITY_TO_ENGLISH.put("도쿄", "Tokyo");
        CITY_TO_ENGLISH.put("오사카", "Osaka");
        CITY_TO_ENGLISH.put("LA", "Los Angeles");
        CITY_TO_ENGLISH.put("파리", "Paris");
        CITY_TO_ENGLISH.put("뉴욕", "New York");
    }

    private static final Map<String, JsonNode> HARDCODED_LOCATIONS = new HashMap<>();

    static {
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        ArrayNode seoulLocations = mapper.createArrayNode();
        ObjectNode seoulAirport = mapper.createObjectNode();
        seoulAirport.put("type", "location");
        seoulAirport.put("subType", "AIRPORT");
        seoulAirport.put("detailedName", "Seoul/KR: Incheon International");
        seoulAirport.put("iataCode", "ICN");
        seoulLocations.add(seoulAirport);
        ObjectNode seoulCity = mapper.createObjectNode();
        seoulCity.put("type", "location");
        seoulCity.put("subType", "CITY");
        seoulCity.put("detailedName", "Seoul/KR");
        seoulCity.put("iataCode", "SEL");
        seoulLocations.add(seoulCity);
        HARDCODED_LOCATIONS.put("Seoul", seoulLocations);

        ArrayNode tokyoLocations = mapper.createArrayNode();
        ObjectNode tokyoAirport = mapper.createObjectNode();
        tokyoAirport.put("type", "location");
        tokyoAirport.put("subType", "AIRPORT");
        tokyoAirport.put("detailedName", "Tokyo/JP: Narita International");
        tokyoAirport.put("iataCode", "NRT");
        tokyoLocations.add(tokyoAirport);
        ObjectNode tokyoCity = mapper.createObjectNode();
        tokyoCity.put("type", "location");
        tokyoCity.put("subType", "CITY");
        tokyoCity.put("detailedName", "Tokyo/JP");
        tokyoCity.put("iataCode", "TYO");
        tokyoLocations.add(tokyoCity);
        HARDCODED_LOCATIONS.put("Tokyo", tokyoLocations);
    }

    public AmadeusClient(
            @Value("${amadeus.client-id}") String clientId,
            @Value("${amadeus.client-secret}") String clientSecret,
            @Value("${amadeus.api.base-url:https://test.api.amadeus.com}") String baseUrl
    ) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
        log.info("Initializing AmadeusClient with clientId: {}, baseUrl: {}", clientId, baseUrl);
    }

    public JsonNode searchLocations(String keyword) throws RuntimeException {
        log.info("Searching locations for keyword: {}", keyword);

        String processedKeyword = CITY_TO_ENGLISH.getOrDefault(keyword, keyword);
        if (!processedKeyword.matches("^[a-zA-Z0-9\\s]+$")) {
            log.error("Invalid keyword: {}. Only English letters, numbers, or spaces are allowed.", processedKeyword);
            throw new RuntimeException("Invalid keyword: " + keyword);
        }

        if (HARDCODED_LOCATIONS.containsKey(processedKeyword)) {
            log.info("Returning hardcoded locations for keyword: {}", processedKeyword);
            return HARDCODED_LOCATIONS.get(processedKeyword);
        }

        try {
            JsonNode response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v1/reference-data/locations")
                            .queryParam("subType", "AIRPORT,CITY")
                            .queryParam("keyword", processedKeyword)
                            .build())
                    .header("Authorization", "Bearer " + getAccessToken())
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), clientResponse -> {
                        return clientResponse.bodyToMono(JsonNode.class)
                                .map(errorBody -> {
                                    String error = errorBody.path("error").asText("Unknown error");
                                    String description = errorBody.path("error_description").asText("No description");
                                    log.error("Location search error: status={}, error={}, description={}",
                                            clientResponse.statusCode(), error, description);
                                    throw new RuntimeException("Amadeus API error: " + description);
                                });
                    })
                    .bodyToMono(JsonNode.class)
                    .block();

            if (response != null && response.has("data")) {
                log.debug("Found {} locations for keyword: {}", response.get("data").size(), processedKeyword);
                return response.get("data");
            } else {
                log.error("Invalid response from Amadeus locations endpoint: {}", response);
                throw new RuntimeException("Invalid locations response");
            }
        } catch (Exception e) {
            log.error("Error during location search for keyword: {}. Cause: {}, Message: {}, StackTrace: {}",
                    processedKeyword, e.getCause(), e.getMessage(), e.getStackTrace(), e);
            try {
                fetchAccessToken();
                JsonNode response = webClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/v1/reference-data/locations")
                                .queryParam("subType", "AIRPORT,CITY")
                                .queryParam("keyword", processedKeyword)
                                .build())
                        .header("Authorization", "Bearer " + getAccessToken())
                        .retrieve()
                        .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), clientResponse -> {
                            return clientResponse.bodyToMono(JsonNode.class)
                                    .map(errorBody -> {
                                        String error = errorBody.path("error").asText("Unknown error");
                                        String description = errorBody.path("error_description").asText("No description");
                                        log.error("Retry location search error: status={}, error={}, description={}",
                                                clientResponse.statusCode(), error, description);
                                        throw new RuntimeException("Amadeus API retry error: " + description);
                                    });
                        })
                        .bodyToMono(JsonNode.class)
                        .block();

                if (response != null && response.has("data")) {
                    log.debug("Retry successful. Found {} locations for keyword: {}", response.get("data").size(), processedKeyword);
                    return response.get("data");
                } else {
                    log.error("Invalid retry response from Amadeus locations endpoint: {}", response);
                    throw new RuntimeException("Invalid retry locations response");
                }
            } catch (Exception retryEx) {
                log.error("Retry failed for location search. Cause: {}, Message: {}, StackTrace: {}",
                        retryEx.getCause(), retryEx.getMessage(), retryEx.getStackTrace(), retryEx);
                throw new RuntimeException("Failed to connect to Amadeus API after retry: " + retryEx.getMessage());
            }
        }
    }

    public synchronized void fetchAccessToken() {
        log.info("Fetching new Amadeus access token");
        try {
            JsonNode response = webClient.post()
                    .uri("/v1/security/oauth2/token")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .bodyValue("grant_type=client_credentials&client_id=" + clientId +
                            "&client_secret=" + clientSecret)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), clientResponse -> {
                        return clientResponse.bodyToMono(JsonNode.class)
                                .map(errorBody -> {
                                    String error = errorBody.path("error").asText("Unknown error");
                                    String description = errorBody.path("error_description").asText("No description");
                                    log.error("Token fetch error: status={}, error={}, description={}",
                                            clientResponse.statusCode(), error, description);
                                    if (error.equals("invalid_client")) {
                                        throw new RuntimeException("Invalid Amadeus client credentials: " + description);
                                    }
                                    throw new RuntimeException("Failed to fetch Amadeus token: " + description);
                                });
                    })
                    .bodyToMono(JsonNode.class)
                    .block();

            if (response != null && response.has("access_token")) {
                accessToken = response.get("access_token").asText();
                tokenExpiryTime = System.currentTimeMillis() + (response.get("expires_in").asLong() * 1000 - 60000);
                log.info("Amadeus access token fetched successfully, expires in {} seconds", response.get("expires_in").asLong());
            } else {
                log.error("Invalid response from Amadeus token endpoint: {}", response);
                throw new RuntimeException("Invalid token response");
            }
        } catch (Exception e) {
            log.error("Failed to fetch Amadeus access token. Cause: {}, Message: {}, StackTrace: {}",
                    e.getCause(), e.getMessage(), e.getStackTrace(), e);
            throw new RuntimeException("Failed to fetch Amadeus token: " + e.getMessage());
        }
    }

    public String getAccessToken() {
        if (accessToken == null || System.currentTimeMillis() >= tokenExpiryTime) {
            fetchAccessToken();
        }
        return accessToken;
    }
}