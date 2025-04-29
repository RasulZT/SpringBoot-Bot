package com.parqour.bot.Enums;

import lombok.Getter;

@Getter
public enum IssueStatusClosed {
    CHECKED("Проверено"),
    CANCELED("Отменено"),
    DUPLICATED("Дубликат");


    public static final String[] ALL = {
            "Проверено",
            "Отменено",
            "Дубликат"
    };
    private final String russianValue;

    IssueStatusClosed(String russianValue) {
        this.russianValue = russianValue;
    }

    public String getRussianValue() {
        return russianValue;
    }

    public static IssueStatusClosed getByRussianValue(String russianValue) {
        for (IssueStatusClosed issueStatusClosed : IssueStatusClosed.values()) {
            if (issueStatusClosed.russianValue.equalsIgnoreCase(russianValue)) {
                return issueStatusClosed;
            }
        }
        return null;
    }
}
