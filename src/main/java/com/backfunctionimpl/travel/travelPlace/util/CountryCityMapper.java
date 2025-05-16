package com.backfunctionimpl.travel.travelPlace.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class CountryCityMapper {
    private static final Map<String, double[]> CITY_MAP = new HashMap<>();

    static {
        CITY_MAP.put("tokyo", new double[]{35.6895, 139.6917});
        CITY_MAP.put("osaka", new double[]{34.6937, 135.5023});

        // 한글 키워드 추가
        CITY_MAP.put("도쿄", new double[]{35.6895, 139.6917});
        CITY_MAP.put("오사카", new double[]{34.6937, 135.5023});
        CITY_MAP.put("후쿠오카", new double[]{33.5904, 130.4017});
        CITY_MAP.put("파리", new double[]{48.8566, 2.3522});
        CITY_MAP.put("로마", new double[]{41.9028, 12.4964});
        CITY_MAP.put("베니스", new double[]{45.4408, 12.3155});
    }

    public static Optional<double[]> getCoordinates(String city) {
        if (city == null) return Optional.empty();
        return Optional.ofNullable(CITY_MAP.get(city.trim()));
    }

    public static Map<String, double[]> getAllCities() {
        return CITY_MAP;
    }


    private static final Map<String, String> CITY_TO_COUNTRY = new HashMap<>();

    static {
        CITY_TO_COUNTRY.put("도쿄", "일본");
        CITY_TO_COUNTRY.put("오사카", "일본");
        CITY_TO_COUNTRY.put("후쿠오카", "일본");
        CITY_TO_COUNTRY.put("파리", "프랑스");
        CITY_TO_COUNTRY.put("로마", "이탈리아");
        CITY_TO_COUNTRY.put("베니스", "이탈리아");
    }

    public static String getCountry(String city) {
        return CITY_TO_COUNTRY.getOrDefault(city, "국가없음");
    }

}
