package com.backfunctionimpl.travel.travelFlight.service;


import com.backfunctionimpl.global.error.CustomException;
import com.backfunctionimpl.global.error.ErrorCode;
import com.backfunctionimpl.travel.travelFlight.dto.FlightSearchReqDto;
import com.backfunctionimpl.travel.travelFlight.dto.FlightSearchResDto;
import com.backfunctionimpl.travel.travelFlight.entity.TravelFlight;
import com.backfunctionimpl.travel.travelFlight.repository.TravelFlightRepository;
import com.backfunctionimpl.travel.travelPlan.entity.TravelPlan;
import com.backfunctionimpl.travel.travelPlan.repository.TravelPlanRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
@Slf4j
public class FlightSearchService {

    @Value("${amadeus.client-id}")
    private String clientId;

    @Value("${amadeus.client-secret}")
    private String clientSecret;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final TravelFlightRepository travelFlightRepository;
    private final TravelPlanRepository travelPlanRepository;
    private String accessToken;
    private long tokenExpiryTime;
    private RestTemplate restTemplate;

    private void refreshAccessToken() {
        String url = "https://test.api.amadeus.com/v1/security/oauth2/token";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");
        body.add("client_id", "YOUR_CLIENT_ID");
        body.add("client_secret", "YOUR_CLIENT_SECRET");
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
        Map<String, Object> responseBody = response.getBody();
        this.accessToken = (String) responseBody.get("access_token");
        this.tokenExpiryTime = System.currentTimeMillis() + ((Integer) responseBody.get("expires_in") * 1000);
    }

    private synchronized String getAccessToken() {
        if (accessToken == null || System.currentTimeMillis() >= tokenExpiryTime) {
            fetchAccessToken();
        }
        return accessToken;
    }

    private void fetchAccessToken() {
        WebClient client = WebClient.builder()
                .baseUrl("https://test.api.amadeus.com")
                .build();

        try {
            JsonNode response = client.post()
                    .uri("/v1/security/oauth2/token")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .bodyValue("grant_type=client_credentials&client_id=" + clientId + "&client_secret=" + clientSecret)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (response != null && response.has("access_token")) {
                accessToken = response.get("access_token").asText();
                tokenExpiryTime = System.currentTimeMillis() + (response.get("expires_in").asLong() * 1000 - 60000);
                log.info("Amadeus access token fetched successfully, expires in {} seconds", response.get("expires_in").asLong());
            } else {
                throw new CustomException(ErrorCode.AMADEUS_API_ERROR);
            }
        } catch (Exception e) {
            log.error("Failed to fetch Amadeus access token: {}", e.getMessage());
            throw new CustomException(ErrorCode.AMADEUS_API_ERROR);
        }
    }

    @Cacheable(value = "flights", key = "#reqDto.origin + '-' + #reqDto.destination + '-' + #reqDto.departureDate", unless = "#reqDto.realTime")
    public FlightSearchResDto searchFlights(FlightSearchReqDto reqDto) {
        log.info("Searching flights for {}", reqDto);
        validateSearchRequest(reqDto);

        String originCode = mapCityToAirportCode(reqDto.getOrigin());
        String destinationCode = mapCityToAirportCode(reqDto.getDestination());
        log.debug("API parameters: originLocationCode={}, destinationLocationCode={}, departureDate={}",
                originCode, destinationCode, reqDto.getDepartureDate());

        WebClient client = WebClient.builder()
                .baseUrl("https://test.api.amadeus.com")
                .defaultHeader("Authorization", "Bearer " + getAccessToken())
                .build();

        try {
            JsonNode response = client.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v2/shopping/flight-offers")
                            .queryParam("originLocationCode", originCode)
                            .queryParam("destinationLocationCode", destinationCode)
                            .queryParam("departureDate", reqDto.getDepartureDate())
                            .queryParam("adults", 1)
                            .queryParam("nonStop", true)
                            .build())
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), clientResponse -> {
                        if (clientResponse.statusCode().value() == 401) {
                            accessToken = null;
                            return Mono.error(new CustomException(ErrorCode.AMADEUS_API_ERROR));
                        } else if (clientResponse.statusCode().value() == 429) {
                            return Mono.error(new CustomException(ErrorCode.AMADEUS_API_ERROR));
                        }
                        return Mono.error(new CustomException(ErrorCode.AMADEUS_API_ERROR));
                    })
                    .bodyToMono(JsonNode.class)
                    .block();
            log.debug("Amadeus API response: {}", response != null ? response.toString() : "null");

            List<FlightSearchResDto.FlightInfo> results = new ArrayList<>();
            if (response != null && response.has("data")) {
                JsonNode data = response.get("data");
                log.debug("Data node: {}", data.toString());
                for (JsonNode flight : data) {
                    FlightSearchResDto.FlightInfo info = new FlightSearchResDto.FlightInfo();
                    if (flight.has("price") && flight.get("price").has("total")) {
                        info.setPrice(flight.get("price").get("total").asText() + " " + flight.get("price").get("currency").asText());
                        if (flight.has("itineraries")) {
                            JsonNode itinerary = flight.get("itineraries").get(0);
                            if (itinerary.has("segments")) {
                                JsonNode segment = itinerary.get("segments").get(0);
                                info.setCarrier(segment.has("carrierCode") ? segment.get("carrierCode").asText() : "Unknown");
                                info.setDepartureTime(segment.has("departure") ? segment.get("departure").get("at").asText() : "Unknown");
                                info.setArrivalTime(segment.has("arrival") ? segment.get("arrival").get("at").asText() : "Unknown");
                            }
                        }
                        results.add(info);
                        log.debug("Flight info: {}", info);
                    } else {
                        log.warn("Invalid flight data: {}", flight.toString());
                    }
                }
            } else {
                log.warn("No data in response: {}", reqDto);
            }

            log.info("Returning {} flights for request: {}", results.size(), reqDto);
            return new FlightSearchResDto(results);
        } catch (WebClientResponseException e) {
            log.error("Amadeus API error: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new CustomException(ErrorCode.AMADEUS_API_ERROR);
        } catch (Exception e) {
            log.error("Amadeus API 호출 실패: {}", e.getMessage());
            throw new CustomException(ErrorCode.AMADEUS_API_ERROR);
        }
    }

    public Long saveFlight(FlightSearchReqDto reqDto, Long travelPlanId) {
        FlightSearchResDto result = searchFlights(reqDto);
        if (result.getFlights().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_FLIGHT_SEARCH);
        }

        TravelPlan travelPlan = travelPlanRepository.findById(travelPlanId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_FLIGHT_SEARCH));

        FlightSearchResDto.FlightInfo flightInfo = result.getFlights().get(0);
        TravelFlight travelFlight = new TravelFlight();
        travelFlight.setTravelPlan(travelPlan);
        travelFlight.setAirlines(flightInfo.getCarrier());
        travelFlight.setDepartureAirport(mapCityToAirportCode(reqDto.getOrigin()));
        travelFlight.setArrivalAirport(mapCityToAirportCode(reqDto.getDestination()));
        travelFlight.setDepartureTime(LocalDateTime.parse(flightInfo.getDepartureTime()));
        travelFlight.setArrivalTime(LocalDateTime.parse(flightInfo.getArrivalTime()));

        travelFlight = travelFlightRepository.save(travelFlight);
        return travelFlight.getId();
    }

    @CacheEvict(value = "flights", key = "#reqDto.origin + '-' + #reqDto.destination + '-' + #reqDto.departureDate")
    public void clearFlightCache(FlightSearchReqDto reqDto) {
        log.info("Clearing cache for {}", reqDto);
    }

    public String mapCityToAirportCode(String city) {
        Map<String, String> cityToAirport = new HashMap<>();
        cityToAirport.put("osaka", "KIX");
        cityToAirport.put("tokyo", "NRT");
        cityToAirport.put("fukuoka", "FUK");
        cityToAirport.put("paris", "CDG");
        cityToAirport.put("rome", "FCO");
        cityToAirport.put("venice", "VCE");
        cityToAirport.put("bangkok", "BKK");
        cityToAirport.put("singapore", "SIN");
        cityToAirport.put("seoul", "ICN");
        return cityToAirport.getOrDefault(city.toLowerCase(), "ICN");
    }

    private void validateSearchRequest(FlightSearchReqDto reqDto) {
        if (reqDto.getOrigin() == null || reqDto.getOrigin().isEmpty() ||
                reqDto.getDestination() == null || reqDto.getDestination().isEmpty() ||
                reqDto.getDepartureDate() == null || reqDto.getDepartureDate().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_FLIGHT_SEARCH);
        }
        try {
            LocalDate departureDate = LocalDate.parse(reqDto.getDepartureDate());
            LocalDate maxDate = LocalDate.now().plusDays(330);
            if (departureDate.isAfter(maxDate)) {
                throw new CustomException(ErrorCode.INVALID_FLIGHT_SEARCH);
            }
            if (departureDate.isBefore(LocalDate.now())) {
                throw new CustomException(ErrorCode.INVALID_FLIGHT_SEARCH);
            }
        } catch (DateTimeParseException e) {
            throw new CustomException(ErrorCode.INVALID_FLIGHT_SEARCH);
        }
    }
}