package com.parqour.bot.Enums;

import com.parqour.bot.Enums.interfaces.Localizable;
import lombok.Getter;

@Getter
public enum YouTrackIssueStatus implements Localizable {
    CREATED("Создано", "Created"),
    UNDER_ANALYSIS("На анализе", "Under Analysis"),
    ASSIGNEE_APPOINTED("Исполнитель назначен", "Assignee Appointed"),
    IN_PROGRESS("В исполнении", "In Progress"),
    COMPLETED("Выполнено", "Completed"),
    VERIFIED("Проверено", "Verified"),
    CANCELED("Отменено", "Canceled"),
    DUPLICATE("Дубликат", "Duplicate"),
    FOR_REVISION("На доработку", "For Revision");

    public static final String[] ALL = {
            "Создано",
            "На анализе",
            "Исполнитель назначен",
            "В исполнении",
            "Выполнено",
            "Проверено",
            "Отменено",
            "Дубликат",
            "На доработку"
    };
    private final String russianValue;
    private final String englishValue;

    YouTrackIssueStatus(String russianValue, String englishValue) {
        this.russianValue = russianValue;
        this.englishValue = englishValue;
    }


    public static YouTrackIssueStatus getByRussianValue(String russianValue) {
        for (YouTrackIssueStatus youTrackIssueStatus : YouTrackIssueStatus.values()) {
            if (youTrackIssueStatus.russianValue.equalsIgnoreCase(russianValue)) {
                return youTrackIssueStatus;
            }
        }
        return null;
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


