package com.parqour.bot.Singleton;

import lombok.*;

@Data
@ToString
@Getter
@Setter
@RequiredArgsConstructor
public class ProjectCustomField {
    private String id;
    private Field field;
    private String $type;

    public void setType(String $type){
        this.$type = $type;
    }
}
