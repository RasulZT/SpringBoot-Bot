package com.parqour.bot.Enums;

import com.parqour.bot.Enums.interfaces.Localizable;
import lombok.Getter;

@Getter
public enum ProblemArea implements Localizable {
    SHLAGBAUM("Шлагбаум", "Barrier"),
    CONTROLLER("Контроллер", "Controller"),
    CALL_PANEL("Вызывная панель", "Call Panel"),
    QR_PANEL("QR панель", "QR Panel"),
    FIXED_CAMERAS("Камеры фикс", "Fixed Cameras"),
    SURVEILLANCE_CAMERA("Камера обзорная", "Surveillance Camera"),
    VIDEO_RECORDER("Видеорегистратор", "Video Recorder"),
    SWITCH("Коммутатор", "Switch"),
    RADOMOSTY("Радиомосты", "Radio Bridges"),
    LOCAL_NETWORK("Локальная Сеть", "Local Network"),
    INTERNET("Интернет", "Internet"),
    OPERATOR_PC("ПК Оператора", "Operator PC"),
    SERVER("Сервер", "Server"),
    RECOGNITION("Распознавание", "Recognition"),
    BILLING_SOFTWARE("Софт - биллинг", "Billing Software"),
    REPORTS("Отчёты", "Reports"),
    TELEGRAM_BOT("Телеграмм бот", "Telegram Bot"),
    PAYMENTS("Платежи", "Payments"),
    MONITORING("Мониторинг", "Monitoring"),
    PHONE("Телефон", "Phone"),
    ATS("АТС", "ATS"),
    FISCALIZATION("Фискализация", "Fiscalization"),
    INTEGRATION("Интеграция с другим ПО", "Integration with other software"),
    MOBILE_APP("Мобильное приложение Паркур", "Parqour Mobile App");


    public static final String[] ALL = {
            "Шлагбаум",
            "Контроллер",
            "Вызывная панель",
            "QR панель",
            "Камеры фикс",
            "Камера обзорная",
            "Видеорегистратор",
            "Коммутатор",
            "Радиомосты",
            "Локальная Сеть",
            "Интернет",
            "ПК Оператора",
            "Сервер",
            "Распознавание",
            "Софт - биллинг",
            "Отчёты",
            "Телеграмм бот",
            "Платежи",
            "Мониторинг",
            "Телефон",
            "АТС",
            "Фискализация",
            "Интеграция с другим ПО",
            "Мобильное приложение Паркур"
    };

    private final String russianValue;
    private final String englishValue;

    ProblemArea(String russianValue, String englishValue) {
        this.russianValue = russianValue;
        this.englishValue = englishValue;
    }

    public static ProblemArea getByRussianValue(String russianValue) {
        for (ProblemArea problemArea : ProblemArea.values()) {
            if (problemArea.russianValue.equalsIgnoreCase(russianValue)) {
                return problemArea;
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