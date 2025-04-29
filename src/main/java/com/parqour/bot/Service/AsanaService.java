package com.parqour.bot.Service;

import com.parqour.bot.Model.AsanaIssueComment;
import com.parqour.bot.entity.TicketEntity;

import java.util.List;
import java.util.Map;

public interface AsanaService {
    String getProjects();

    boolean getTasks();

    List<String> getCustomFieldsString();

    Map<String, Map<String, String>> getCustomFields();

    String createIssue(TicketEntity ticket);

    StringBuilder updateAsanaComment(String asanaIssueId);

    String getIssueStatus(String issueId);

    String getIssueSection(String issueId);

    List<AsanaIssueComment> getIssueComments(String issueId);
}
