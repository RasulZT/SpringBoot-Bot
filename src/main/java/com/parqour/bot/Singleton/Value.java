package com.parqour.bot.Singleton;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Data
@ToString
@Getter
@Setter
public class Value {

    private String id;
    private String name;
    private String $type;

    public void setType(String $type){
        this.$type = $type;
    }
}
