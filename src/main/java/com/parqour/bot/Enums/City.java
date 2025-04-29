package com.parqour.bot.Enums;

import lombok.Getter;

@Getter
public enum City {
    ALMATY("АЛМАТЫ"),
    ASTANA("АСТАНА"),
    SHYMKENT("ШЫМКЕНТ"),
    OTHER("ДРУГОЙ");

    public static final String[] ALL = {"АЛМАТЫ",
            "АСТАНА",
            "ШЫМКЕНТ",
            "ДРУГОЙ"};
    private final String russianValue;

    City(String russianValue) {
        this.russianValue = russianValue;
    }

    public String getRussianValue() {
        return russianValue;
    }

    public static City getByRussianValue(String russianValue) {
        for (City city : City.values()) {
            if (city.russianValue.equalsIgnoreCase(russianValue)) {
                return city;
            }
        }
        throw new IllegalArgumentException("Invalid Russian value: " + russianValue);
    }
}
