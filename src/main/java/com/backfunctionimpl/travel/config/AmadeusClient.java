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

import java.time.Duration;
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
        CITY_TO_ENGLISH.put("뉴 욕", "New York");
        CITY_TO_ENGLISH.put("로스앤젤레스", "Los Angeles");
        CITY_TO_ENGLISH.put("런던", "London");
        CITY_TO_ENGLISH.put("싱가포르", "Singapore");
    }

    private static final Map<String, JsonNode> HARDCODED_LOCATIONS = new HashMap<>();
    static {
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        // Seoul
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

        // Tokyo
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

        // Paris
        ArrayNode parisLocations = mapper.createArrayNode();
        ObjectNode parisAirport = mapper.createObjectNode();
        parisAirport.put("type", "location");
        parisAirport.put("subType", "AIRPORT");
        parisAirport.put("detailedName", "Paris/FR: Charles de Gaulle");
        parisAirport.put("iataCode", "CDG");
        parisLocations.add(parisAirport);
        ObjectNode parisCity = mapper.createObjectNode();
        parisCity.put("type", "location");
        parisCity.put("subType", "CITY");
        parisCity.put("detailedName", "Paris/FR");
        parisCity.put("iataCode", "PAR");
        parisLocations.add(parisCity);
        HARDCODED_LOCATIONS.put("Paris", parisLocations);

        // New York
        ArrayNode newYorkLocations = mapper.createArrayNode();
        ObjectNode jfkAirport = mapper.createObjectNode();
        jfkAirport.put("type", "location");
        jfkAirport.put("subType", "AIRPORT");
        jfkAirport.put("detailedName", "New York/US: John F. Kennedy International");
        jfkAirport.put("iataCode", "JFK");
        newYorkLocations.add(jfkAirport);
        ObjectNode nycCity = mapper.createObjectNode();
        nycCity.put("type", "location");
        nycCity.put("subType", "CITY");
        nycCity.put("detailedName", "New York/US");
        nycCity.put("iataCode", "NYC");
        newYorkLocations.add(nycCity);
        HARDCODED_LOCATIONS.put("New York", newYorkLocations);

        // Los Angeles
        ArrayNode losAngelesLocations = mapper.createArrayNode();
        ObjectNode laxAirport = mapper.createObjectNode();
        laxAirport.put("type", "location");
        laxAirport.put("subType", "AIRPORT");
        laxAirport.put("detailedName", "Los Angeles/US: Los Angeles International");
        laxAirport.put("iataCode", "LAX");
        losAngelesLocations.add(laxAirport);
        ObjectNode laCity = mapper.createObjectNode();
        laCity.put("type", "location");
        laCity.put("subType", "CITY");
        laCity.put("detailedName", "Los Angeles/US");
        laCity.put("iataCode", "LAX");
        losAngelesLocations.add(laCity);
        HARDCODED_LOCATIONS.put("Los Angeles", losAngelesLocations);
        // London
        ArrayNode londonLocations = mapper.createArrayNode();
        ObjectNode lhrAirport = mapper.createObjectNode();
        lhrAirport.put("type", "location");
        lhrAirport.put("subType", "AIRPORT");
        lhrAirport.put("detailedName", "London/GB: Heathrow");
        lhrAirport.put("iataCode", "LHR");
        londonLocations.add(lhrAirport);
        ObjectNode lonCity = mapper.createObjectNode();
        lonCity.put("type", "location");
        lonCity.put("subType", "CITY");
        lonCity.put("detailedName", "London/GB");
        lonCity.put("iataCode", "LON");
        londonLocations.add(lonCity);
        HARDCODED_LOCATIONS.put("London", londonLocations);

        // Singapore
        ArrayNode singaporeLocations = mapper.createArrayNode();
        ObjectNode sinAirport = mapper.createObjectNode();
        sinAirport.put("type", "location");
        sinAirport.put("subType", "AIRPORT");
        sinAirport.put("detailedName", "Singapore/SG: Changi");
        sinAirport.put("iataCode", "SIN");
        singaporeLocations.add(sinAirport);
        ObjectNode sinCity = mapper.createObjectNode();
        sinCity.put("type", "location");
        sinCity.put("subType", "CITY");
        sinCity.put("detailedName", "Singapore/SG");
        sinCity.put("iataCode", "SIN");
        singaporeLocations.add(sinCity);
        HARDCODED_LOCATIONS.put("Singapore", singaporeLocations);
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
        if (keyword == null || keyword.trim().isEmpty()) {
            log.error("Empty or null keyword provided");
            throw new RuntimeException("Keyword cannot be empty");
        }

        // 키워드 정규화
        final String processedKeyword = CITY_TO_ENGLISH.getOrDefault(keyword.trim(), keyword.trim())
                .replaceAll("\\s+", " ");
        log.debug("Processed keyword: {} (original: {})", processedKeyword, keyword);

        // 하드코딩된 데이터 체크 (대소문자 무시)
        String matchedKey = HARDCODED_LOCATIONS.keySet().stream()
                .filter(key -> key.equalsIgnoreCase(processedKeyword))
                .findFirst()
                .orElse(null);
        if (matchedKey != null) {
            log.info("Returning hardcoded locations for keyword: {}", matchedKey);
            return HARDCODED_LOCATIONS.get(matchedKey);
        }

        // 공항 코드 직접 처리
        String upperKeyword = processedKeyword.toUpperCase();
        if (upperKeyword.matches("^[A-Z]{3}$")) {
            log.debug("Direct IATA code detected: {}", upperKeyword);
            ArrayNode codeResult = new com.fasterxml.jackson.databind.ObjectMapper().createArrayNode();
            ObjectNode codeNode = codeResult.addObject();
            codeNode.put("type", "location");
            codeNode.put("subType", "AIRPORT");
            codeNode.put("detailedName", upperKeyword);
            codeNode.put("iataCode", upperKeyword);
            return codeResult;
        }

        // 키워드 유효성 검사
        if (!processedKeyword.matches("^[a-zA-Z0-9\\s]+$")) {
            log.error("Invalid keyword: {}. Only English letters, numbers, or spaces are allowed.", processedKeyword);
            throw new RuntimeException("Invalid keyword: " + keyword);
        }

        // API 호출
        return fetchLocationsFromApi(processedKeyword);
    }

    private JsonNode fetchLocationsFromApi(String processedKeyword) {
        try {
            JsonNode response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v1/reference-data/locations")
                            .queryParam("subType", "AIRPORT,CITY")
                            .queryParam("keyword", processedKeyword.toUpperCase())
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
                    .timeout(Duration.ofSeconds(10))
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
                    processedKeyword, e.getCause(), e.getMessage(), e.getStackTrace());
            for (int i = 0; i < 5; i++) {
                try {
                    log.info("Retrying location search, attempt {}", i + 1);
                    Thread.sleep(2000 * (i + 1));
                    fetchAccessToken();
                    JsonNode response = webClient.get()
                            .uri(uriBuilder -> uriBuilder
                                    .path("/v1/reference-data/locations")
                                    .queryParam("subType", "AIRPORT,CITY")
                                    .queryParam("keyword", processedKeyword.toUpperCase())
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
                            .timeout(Duration.ofSeconds(10))
                            .block();

                    if (response != null && response.has("data")) {
                        log.debug("Retry successful. Found {} locations for keyword: {}", response.get("data").size(), processedKeyword);
                        return response.get("data");
                    }
                } catch (Exception retryEx) {
                    log.error("Retry {} failed for location search. Cause: {}, Message: {}, StackTrace: {}",
                            i + 1, retryEx.getCause(), retryEx.getMessage(), retryEx.getStackTrace());
                }
            }
            throw new RuntimeException("Failed to connect to Amadeus API after retries: " + e.getMessage());
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
                    .timeout(Duration.ofSeconds(10))
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
                    e.getCause(), e.getMessage(), e.getStackTrace());
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