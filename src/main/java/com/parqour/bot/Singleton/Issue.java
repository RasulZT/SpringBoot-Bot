package com.parqour.bot.Singleton;

import com.nimbusds.jose.shaded.json.JSONObject;
import lombok.*;

import java.util.List;

@Data
@ToString
@Getter
@Setter
public class Issue {

    private String id;
    private JSONObject project;
    private String summary;
    private String description;
    private String $type;
    private List<CustomField> customFields;

    public void setType(String $type){
        this.$type = $type;
    }
}
