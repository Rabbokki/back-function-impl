package com.backfunctionimpl.travel.travelPlace.dto;

import lombok.Data;

import java.util.List;

@Data
public class PlaceResult {
    private String name;
    private String place_id;
    private Geometry geometry;
    private String vicinity;
    private double rating;
    private int user_ratings_total;
    private String business_status;
    private List<Photo> photos;

    // 내부 클래스 for 위치 정보
    @Data
    public static class Geometry {
        private Location location;

        @Data
        public static class Location {
            private double lat;
            private double lng;
        }
    }
}
