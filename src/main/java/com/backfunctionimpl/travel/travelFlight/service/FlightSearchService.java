package com.backfunctionimpl.travel.travelFlight.service;


import com.backfunctionimpl.travel.travelFlight.dto.FlightSearchReqDto;
import com.backfunctionimpl.travel.travelFlight.dto.FlightSearchResDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class FlightSearchService {

    @Value("${skyscanner.api-key}")
    private String apiKey;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public FlightSearchResDto searchFlights(FlightSearchReqDto reqDto) {
        WebClient client = WebClient.builder()
                .baseUrl("https://skyscanner-skyscanner-flight-search-v1.p.rapidapi.com")
                .defaultHeader("X-RapidAPI-Key", apiKey)
                .defaultHeader("X-RapidAPI-Host", "skyscanner-skyscanner-flight-search-v1.p.rapidapi.com")
                .build();

        try {
            // 세션 생성
            Mono<String> sessionResponse = client.post()
                    .uri("/apiservices/pricing/v1.0")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .bodyValue(String.format(
                            "country=KR&currency=USD&locale=en-US&originPlace=%s-sky&destinationPlace=%s-sky&outboundDate=%s&adults=%d",
                            reqDto.getOrigin(), reqDto.getDestination(), reqDto.getDepartureDate(), 1
                    ))
                    .retrieve()
                    .toBodilessEntity()
                    .map(response -> response.getHeaders().getLocation().toString());

            String sessionKey = sessionResponse.block().split("/")[5];

            // 결과 조회
            JsonNode response = client.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/apiservices/pricing/uk2/v1.0/{sessionKey}")
                            .queryParam("pageIndex", 0)
                            .queryParam("pageSize", 10)
                            .build(sessionKey))
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            List<FlightSearchResDto.FlightInfo> results = new ArrayList<>();

            if (response != null && response.has("Itineraries")) {
                for (JsonNode itinerary : response.get("Itineraries")) {
                    FlightSearchResDto.FlightInfo info = new FlightSearchResDto.FlightInfo();
                    JsonNode pricingOption = itinerary.get("PricingOptions").get(0);
                    info.setPrice(pricingOption.get("Price").asText() + " USD");
                    // 항공사 및 기타 정보는 응답 구조에 따라 추가 매핑
                    results.add(info);
                }
            }

            return new FlightSearchResDto(results);
        } catch (Exception e) {
            throw new RuntimeException("Skyscanner API 호출 실패: " + e.getMessage());
        }
    }
}
