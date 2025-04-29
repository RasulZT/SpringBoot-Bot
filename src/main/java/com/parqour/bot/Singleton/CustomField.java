package com.parqour.bot.Singleton;

import lombok.*;

@Data
@ToString
@Getter
@Setter
@RequiredArgsConstructor
public class CustomField {
    private String id;
    private ProjectCustomField projectCustomField;
    private Value value;

    private String $type;

    public void setType(String $type){
        this.$type = $type;
    }

//    public CustomFields(Value value, String name, String type){
//        this.type = type;
//        this.name = name;
//        this.value = value;
//    }
}
