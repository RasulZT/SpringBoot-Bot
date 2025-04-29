package com.parqour.bot.Model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.parqour.bot.Singleton.Author;
import lombok.Data;

@Data
public class AsanaIssueComment {
    private String gid;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("created_by")
    private Author createdBy;

    @JsonProperty("resource_type")
    private String resourceType;
    private String text;
    private String type;
    private String resource_subtype;
}
