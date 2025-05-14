//package com.backfunctionimpl.travel.travelPlan.dto;
//
//import lombok.Data;
//import java.time.LocalDate;
//import java.util.List;
//
//@Data
//public class TravelPlanSaveRequestDto {
//    private Long accountId;
//    private LocalDate startDate;
//    private LocalDate endDate;
//    private String country;
//    private String city;
//    private String transportation;
//    private List<PlaceDto> places;
//    private List<AccommodationDto> accommodations;
//
//    @Data
//    public static class PlaceDto {
//        private String day;
//        private String name;
//        private String category;
//        private String description;
//        private String time;
//        private double lat;
//        private double lng;
//    }
//
//    @Data
//    public static class AccommodationDto {
//        private String day;
//        private String name;
//        private String description;
//        private double lat;
//        private double lng;
//    }
//}
