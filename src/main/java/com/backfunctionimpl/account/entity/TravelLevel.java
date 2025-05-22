package com.backfunctionimpl.account.entity;

public enum TravelLevel {
    BEGINNER("여행 새싹", 0, 99),
    NOVICE("초보 여행자", 100, 199),
    EXPLORER("탐험가", 200, 299),
    ADVENTURER("모험가", 300, 399),
    WORLD_TRAVELER("세계 여행자", 400, 499),
    MASTER("여행 달인", 500, 599),
    LEGEND("전설의 여행자", 600, Integer.MAX_VALUE); // 끝판왕은 무한대! 헐

    private final String label;
    private final int minExp;
    private final int maxExp;

    TravelLevel(String label, int minExp, int maxExp) {
        this.label = label;
        this.minExp = minExp;
        this.maxExp = maxExp;
    }

    public String getLabel() {
        return label;
    }

    public int getMinExp() {
        return minExp;
    }

    public int getMaxExp() {
        return maxExp;
    }

    // 🔥 경험치로 레벨 찾는 메서드 추가
    public static TravelLevel findByExp(int exp) {
        for (TravelLevel level : TravelLevel.values()) {
            if (exp >= level.minExp && exp <= level.maxExp) {
                return level;
            }
        }
        return BEGINNER; // 기본값
    }
}
