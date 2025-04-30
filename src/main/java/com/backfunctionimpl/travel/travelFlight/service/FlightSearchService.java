package com.backfunctionimpl.travel.travelFlight.service;


import com.backfunctionimpl.global.error.CustomException;
import com.backfunctionimpl.global.error.ErrorCode;
import com.backfunctionimpl.travel.travelFlight.dto.FlightSearchReqDto;
import com.backfunctionimpl.travel.travelFlight.dto.FlightSearchResDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
@Slf4j
public class FlightSearchService {

    @Value("${skyscanner.api-key}")
    private String apiKey;

    @Value("${skyscanner.api-host}")
    private String apiHost;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Cacheable(value = "flights", key = "#reqDto.origin + '-' + #reqDto.destination + '-' + #reqDto.departureDate", unless = "#reqDto.realTime")
    public FlightSearchResDto searchFlights(FlightSearchReqDto reqDto) {
        log.info("Searching flights for {}", reqDto);
        validateSearchRequest(reqDto);

        String originCode = mapCityToAirportCode(reqDto.getOrigin());
        String destinationCode = mapCityToAirportCode(reqDto.getDestination());
        Map<String, String> originIds = getAirportIds(reqDto.getOrigin());
        Map<String, String> destinationIds = getAirportIds(reqDto.getDestination());
        log.debug("API parameters: origin={}, originId={}, destination={}, destinationId={}, outboundDate={}",
                originCode, originIds.get("id"), destinationCode, destinationIds.get("id"), reqDto.getDepartureDate());

        WebClient client = WebClient.builder()
                .baseUrl("https://" + apiHost)
                .defaultHeader("x-rapidapi-key", apiKey)
                .defaultHeader("x-rapidapi-host", apiHost)
                .build();

        try {
            JsonNode response = client.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/flights/one-way/list")
                            .queryParam("origin", originCode.replace("-sky", ""))
                            .queryParam("originId", originIds.get("id"))
                            .queryParam("destination", destinationCode.replace("-sky", ""))
                            .queryParam("destinationId", destinationIds.get("id"))
                            .queryParam("outboundDate", reqDto.getDepartureDate())
                            .build())
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, clientResponse ->
                            Mono.error(new CustomException(ErrorCode.SKYSCANNER_API_ERROR)))
                    .bodyToMono(JsonNode.class)
                    .block();
            log.debug("Skyscanner API response: {}", response != null ? response.toString() : "null");

            List<FlightSearchResDto.FlightInfo> results = new ArrayList<>();
            if (response != null && response.has("data")) {
                JsonNode data = response.get("data");
                log.debug("Data node: {}", data.toString());
                if (data.has("flightQuotes")) {
                    JsonNode flightQuotes = data.get("flightQuotes");
                    log.debug("FlightQuotes node: {}", flightQuotes.toString());
                    if (flightQuotes.has("results")) {
                        for (JsonNode flight : flightQuotes.get("results")) {
                            FlightSearchResDto.FlightInfo info = new FlightSearchResDto.FlightInfo();
                            if (flight.has("content") && flight.get("content").has("price")) {
                                info.setPrice(flight.get("content").get("price").asText());
                                results.add(info);
                                log.debug("Flight info: {}", info);
                            } else {
                                log.warn("Invalid flight data: {}", flight.toString());
                            }
                        }
                    } else {
                        log.warn("No results in flightQuotes: {}", flightQuotes.toString());
                    }
                } else {
                    log.warn("No flightQuotes in data: {}", data.toString());
                }
            } else {
                log.warn("No data in response: {}", reqDto);
            }

            log.info("Returning {} flights for request: {}", results.size(), reqDto);
            return new FlightSearchResDto(results);
        } catch (WebClientResponseException e) {
            log.error("Skyscanner API error: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new CustomException(ErrorCode.SKYSCANNER_API_ERROR);
        } catch (Exception e) {
            log.error("Skyscanner API 호출 실패: {}", e.getMessage());
            throw new CustomException(ErrorCode.SKYSCANNER_API_ERROR);
        }
    }
    @CacheEvict(value = "flights", key = "#reqDto.origin + '-' + #reqDto.destination + '-' + #reqDto.departureDate")
    public void clearFlightCache(FlightSearchReqDto reqDto) {
        log.info("Clearing cache for {}", reqDto);
    }

    public String mapCityToAirportCode(String city) {
        Map<String, String> cityToAirport = new HashMap<>();
        cityToAirport.put("osaka", "KIX-sky");
        cityToAirport.put("tokyo", "NRT-sky");
        cityToAirport.put("fukuoka", "FUK-sky");
        cityToAirport.put("paris", "CDG-sky");
        cityToAirport.put("rome", "FCO-sky");
        cityToAirport.put("venice", "VCE-sky");
        cityToAirport.put("bangkok", "BKK-sky");
        cityToAirport.put("singapore", "SIN-sky");
        cityToAirport.put("seoul", "ICN-sky");
        return cityToAirport.getOrDefault(city.toLowerCase(), "ICN-sky");
    }
    public Map<String, String> getAirportIds(String city) {
        Map<String, String> result = new HashMap<>();
        result.put("code", mapCityToAirportCode(city).replace("-sky", ""));
        result.put("id", switch (city.toLowerCase()) {
            case "seoul" -> "27537542"; // ICN
            case "tokyo" -> "95673827"; // NRT
            default -> "27537542";
        });
        return result;
    }

//    public Map<String, String> getAirportIds(String city) {
//        WebClient client = WebClient.builder()
//                .baseUrl("https://" + apiHost)
//                .defaultHeader("x-rapidapi-key", apiKey)
//                .defaultHeader("x-rapidapi-host", apiHost)
//                .build();
//
//        try {
//            JsonNode response = client.get()
//                    .uri(uriBuilder -> uriBuilder
//                            .path("/airports")
//                            .queryParam("city", city)
//                            .build())
//                    .retrieve()
//                    .bodyToMono(JsonNode.class)
//                    .block();
//
//            Map<String, String> result = new HashMap<>();
//            if (response != null && response.has("airports")) {
//                JsonNode airport = response.get("airports").get(0);
//                result.put("code", airport.get("code").asText());
//                result.put("id", airport.get("id").asText());
//            } else {
//                log.warn("No airports found for city: {}", city);
//                result.put("code", mapCityToAirportCode(city).replace("-sky", ""));
//                result.put("id", "27537542"); // 기본 ID
//            }
//            return result;
//        } catch (Exception e) {
//            log.error("Failed to fetch airport IDs for city: {}, error: {}", city, e.getMessage());
//            Map<String, String> result = new HashMap<>();
//            result.put("code", mapCityToAirportCode(city).replace("-sky", ""));
//            result.put("id", "27537542"); // 기본 ID
//            return result;
//        }
//    }

    private void validateSearchRequest(FlightSearchReqDto reqDto) {
        if (reqDto.getOrigin() == null || reqDto.getOrigin().isEmpty() ||
                reqDto.getDestination() == null || reqDto.getDestination().isEmpty() ||
                reqDto.getDepartureDate() == null || reqDto.getDepartureDate().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_FLIGHT_SEARCH);
        }
        try {
            LocalDate.parse(reqDto.getDepartureDate());
        } catch (DateTimeParseException e) {
            throw new CustomException(ErrorCode.INVALID_FLIGHT_SEARCH);
        }
    }
}