package com.parqour.bot.Singleton;

import lombok.*;

@Data
@ToString
@Getter
@Setter
@RequiredArgsConstructor
public class Field {
    private String id;
    private String name;
    private String $type;

    public void setType(String $type){
        this.$type = $type;
    }
}
