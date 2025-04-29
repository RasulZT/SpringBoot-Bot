package com.parqour.bot.Service;

import com.parqour.bot.Singleton.IssueComment;
import com.parqour.bot.Singleton.Ticket;
import com.parqour.bot.entity.TicketEntity;

import java.util.List;

public interface YouTrackService {
    boolean getProjects();

    boolean getIssues();

    List<String> getCustomFields();

    String createIssue(TicketEntity ticket);

    String getIssueStatus(String issueId);

    List<IssueComment> getIssueComments(String issueId);
}
