package com.parqour.bot.Singleton;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@ToString
@Getter
@Setter
@RequiredArgsConstructor
public class Author {
    private String name;

    @JsonProperty("resource_type")
    private String resourceType;
    private String gid;
}
