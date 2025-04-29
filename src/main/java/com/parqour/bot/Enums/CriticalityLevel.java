package com.parqour.bot.Enums;

import com.parqour.bot.Enums.interfaces.Localizable;
import lombok.Getter;

@Getter
public enum CriticalityLevel implements Localizable {
    EVENT("Событие", "Event"),
    INCIDENT("Инцидент", "Incident");

    public static final String[] ALL_CALLBACK = {
            "EVENT",
            "INCIDENT",
    };

    public static final String[] ALL_RU = {
            "Событие",
            "Инцидент",
    };

    public static final String[] ALL_EN = {
            "Event",
            "Incident",
    };
    private final String russianValue;
    private final String englishValue;

    CriticalityLevel(String russianValue, String englishValue) {
        this.russianValue = russianValue;
        this.englishValue = englishValue;
    }
    public static CriticalityLevel getByRussianValue(String russianValue) {
        for (CriticalityLevel criticalityLevel : CriticalityLevel.values()) {
            if (criticalityLevel.russianValue.equalsIgnoreCase(russianValue)) {
                return criticalityLevel;
            }
        }
        throw new IllegalArgumentException("Invalid Russian value: " + russianValue);
    }

    @Override
    public String getLocalizedValue(String languageCode) {
        switch (languageCode) {
            case "ru":
                return getRussianValue();
            case "en":
                return getEnglishValue();
            default:
                return getRussianValue();
        }
    }
}
