package com.parqour.bot.Model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AsanaTask {
    private String gid;
    private boolean completed;

    @JsonProperty("created_at")
    private String createdAt;

    private List<Membership> memberships;

    @JsonProperty("custom_fields")
    private List<CustomField> customFields;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Membership {
        private Section section;
        private Project project;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Section {
        private String gid;
        private String name;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Project {
        private String gid;
        private String name;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CustomField {
        private String gid;
        private String name;

        @JsonProperty("enum_value")
        private EnumValue enumValue;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EnumValue {
        private String gid;
        private String name;
    }
}

