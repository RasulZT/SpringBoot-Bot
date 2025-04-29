package com.parqour.bot.Enums;

import com.parqour.bot.Enums.interfaces.Localizable;
import lombok.Getter;

@Getter
public enum Role implements Localizable {
    OPERATOR("Оператор", "Operator"),
    PARKING_ADMIN("Администратор", "Parking Administrator");

    public static final String[] ALL = {"Оператор",
            "Администратор"};
    private final String russianValue;
    private final String englishValue;

    Role(String russianValue, String englishValue) {
        this.russianValue = russianValue;
        this.englishValue = englishValue;
    }

    public static Role getByRussianValue(String russianValue) {
        for (Role role : Role.values()) {
            if (role.russianValue.equalsIgnoreCase(russianValue)) {
                return role;
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
