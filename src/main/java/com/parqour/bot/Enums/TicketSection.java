package com.parqour.bot.Enums;

import com.parqour.bot.Enums.interfaces.Localizable;
import lombok.Getter;

import java.util.EnumSet;
import java.util.Set;

@Getter
public enum TicketSection implements Localizable {
    NEW("Новая", "New"),
    L2("Вторая линия", "Line-2"),
    L3("Третья линия", "Line-3"),
    CANCELLED("Отменено", "Cancelled"),
    DONE("Завершено", "Done");

    public static final String[] ALL = {
            "Новая",
            "Вторая линия",
            "Третья линия",
            "Отменено",
            "Завершено"
    };

    private static final Set<TicketSection> WORK_SECTIONS = EnumSet.of(NEW, L2, L3, DONE, CANCELLED);

    public boolean isWorkSection() {
        return WORK_SECTIONS.contains(this);
    }

    private final String russianValue;
    private final String englishValue;

    TicketSection(String russianValue, String englishValue) {
        this.russianValue = russianValue;
        this.englishValue = englishValue;
    }

    public static TicketSection getByRussianValue(String russianValue) {
        for (TicketSection ticketSection : TicketSection.values()) {
            if (ticketSection.russianValue.equalsIgnoreCase(russianValue)) {
                return ticketSection;
            }
        }
        return null;
    }

    public static TicketSection getByValue(String value) {
        if (value == null) return null;
        for (TicketSection section : TicketSection.values()) {
            if (section.russianValue.equalsIgnoreCase(value) || section.englishValue.equalsIgnoreCase(value)) {
                return section;
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
