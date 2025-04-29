package com.parqour.bot.Enums;

import com.parqour.bot.Enums.interfaces.Localizable;
import lombok.Getter;

@Getter
public enum AsanaIssueStatus implements Localizable {
    CREATED("Создано", "Created"),
    UNDER_ANALYSIS("На анализе", "Under Analysis"),
    ASSIGNEE_APPOINTED("Исполнитель назначен", "Assignee Appointed"),
    IN_PROGRESS("В исполнении", "In Progress"),
    COMPLETED("Выполнено", "Completed"),
    NOT_COMPLETED("Не выполнено", "Not Completed"),
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
            "Не выполнено",
            "Проверено",
            "Отменено",
            "Дубликат",
            "На доработку"
    };
    private final String russianValue;
    private final String englishValue;

    AsanaIssueStatus(String russianValue, String englishValue) {
        this.russianValue = russianValue;
        this.englishValue = englishValue;
    }

    public static AsanaIssueStatus getByRussianValue(String russianValue) {
        for (AsanaIssueStatus youTrackIssueStatus : AsanaIssueStatus.values()) {
            if (youTrackIssueStatus.russianValue.equalsIgnoreCase(russianValue)) {
                return youTrackIssueStatus;
            }
        }
        return null;
    }

    public static AsanaIssueStatus getByValue(String value) {
        for (AsanaIssueStatus status : AsanaIssueStatus.values()) {
            if (status.russianValue.equalsIgnoreCase(value) || status.englishValue.equalsIgnoreCase(value)) {
                return status;
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
