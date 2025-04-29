package com.parqour.bot.Enums;

import com.parqour.bot.Enums.interfaces.Localizable;
import lombok.Getter;

@Getter
public enum ProblemFrequency implements Localizable {
    ONE_TIME("Разовая проблема", "One-time problem"),
    PERIODIC("Периодический повторяющаяся", "Periodically recurring");


    public static final String[] ALL = {
            "Разовая проблема",
            "Периодический повторяющаяся",
    };
    private final String russianValue;
    private final String englishValue;

    ProblemFrequency(String russianValue, String englishValue) {
        this.russianValue = russianValue;
        this.englishValue = englishValue;
    }

    public static ProblemFrequency getByRussianValue(String russianValue) {
        for (ProblemFrequency problemFrequency : ProblemFrequency.values()) {
            if (problemFrequency.russianValue.equalsIgnoreCase(russianValue)) {
                return problemFrequency;
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
