package com.parqour.bot.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Getter
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AsanaCustomField {
    private Map<String, Object> fields = new HashMap<>();

    public void addField(String gid, Object value) {
        fields.put(gid, value);
    }

}
