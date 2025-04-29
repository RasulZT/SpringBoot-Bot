package com.parqour.bot.response;

import lombok.*;

@Data
@ToString
@Getter
@Setter
public class Bundle {
    private BundleValue[] values;
    // getter and setter
}
