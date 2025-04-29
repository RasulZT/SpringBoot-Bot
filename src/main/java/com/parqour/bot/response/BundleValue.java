package com.parqour.bot.response;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Data
@ToString
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class BundleValue {
    private String name;
}
