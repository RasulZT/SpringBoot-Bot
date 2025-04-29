package com.parqour.bot.Enums;

import com.parqour.bot.Enums.interfaces.Localizable;
import lombok.Getter;

@Getter
public enum IncidentProblems implements Localizable {
    FIX_FULLY_DROPPED("Фикса полностью слетела", "Fix fully dropped"),
    SOFTWARE_CRASHED("Софт упал", "Software crashed"),
    KASPI_PAYMENTS_UNAVAILABLE("Каспи платежи не доступны", "Kaspi payments unavailable"),
    OVERLOADED_POST_BROKEN("Нагруженный пост сломался", "Overloaded post broken"),
    INTERNET_DISCONNECTED("Интернет выключился", "Internet disconnected"),
    OTHER("Другое", "Other");


    public static final String[] ALL = {
            "Фикса полностью слетела",
            "Софт упал",
            "Каспи платежи не доступны",
            "Нагруженный пост сломался",
            "Интернет выключился",
            "Другое"
    };
    private final String russianValue;
    private final String englishValue;

    IncidentProblems(String russianValue, String englishValue) {
        this.russianValue = russianValue;
        this.englishValue = englishValue;
    }

    public static IncidentProblems getByRussianValue(String russianValue) {
        for (IncidentProblems incidentProblem : IncidentProblems.values()) {
            if (incidentProblem.russianValue.equalsIgnoreCase(russianValue)) {
                return incidentProblem;
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
