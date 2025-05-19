package com.backfunctionimpl.travel.travelPlan.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class AiPlanRequest {
    @NotBlank(message = "목적지가 누락되었습니다.")
    private String destination;
    private String preferences;
    private int budget;
    private int pace;
    @NotBlank(message = "시작 날짜가 누락되었습니다.")
    private String startDate;
    @NotBlank(message = "종료 날짜가 누락되었습니다.")
    private String endDate;
    private List<Map<String, Object>> itinerary; // @NotEmpty 제거
    private String userId;

    // 호환성을 위한 임시 필드
    private String start_date;
    private String end_date;

    // start_date, end_date를 startDate, endDate로 매핑
    public void setStart_date(String start_date) {
        this.start_date = start_date;
        if (start_date != null && !start_date.trim().isEmpty()) {
            this.startDate = start_date;
        }
    }

    public void setEnd_date(String end_date) {
        this.end_date = end_date;
        if (end_date != null && !end_date.trim().isEmpty()) {
            this.endDate = end_date;
        }
    }
}