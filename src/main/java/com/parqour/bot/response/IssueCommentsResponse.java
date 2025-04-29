package com.parqour.bot.response;

import com.parqour.bot.Model.AsanaIssueComment;
import lombok.Data;

import java.util.List;

@Data
public class IssueCommentsResponse {
    private List<AsanaIssueComment> data;
}
