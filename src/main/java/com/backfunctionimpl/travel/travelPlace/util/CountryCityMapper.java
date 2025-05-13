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

    // 디버깅용: 현재 등록된 키들을 모두 출력
    public static Set<String> getAvailableCities() {
        return CITY_MAP.keySet();
    }
}
