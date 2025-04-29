package com.parqour.bot.Enums;

import com.parqour.bot.Enums.interfaces.Localizable;
import lombok.Getter;

@Getter
public enum EventProblems implements Localizable {
    FIX_MASK_HANGS_FLARE("Фикса (маска, зависания, засвет)", "Fix (mask, hangs, flare)"),
    CALLS_NOT_WORKING("Звонки не работают", "Calls not working"),
    INCORRECT_GATE_PROCESSING("Некорректная отработка шлагбаума", "Incorrect gate processing"),
    SOFTWARE_BUG("Баг", "Software bug"),
    ENGINEER_REQUIRED("Нужен инженер", "Engineer required"),
    OTHER("Другое", "Other");

    private final String russianValue;
    private final String englishValue;

    EventProblems(String russianValue, String englishValue) {
        this.russianValue = russianValue;
        this.englishValue = englishValue;
    }

    public String getLocalizedValue(String languageCode) {
        switch (languageCode) {
            case "ru":
                return getRussianValue();
            case "en":
                return getEnglishValue();
            default:
                throw new IllegalArgumentException("Unsupported language code: " + languageCode);
        }
    }

}

