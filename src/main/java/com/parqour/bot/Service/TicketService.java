package com.parqour.bot.Service;

import com.parqour.bot.Model.AsanaIssueComment;
import com.parqour.bot.Singleton.IssueComment;
import com.parqour.bot.Singleton.Ticket;
import com.parqour.bot.entity.AsanaIssueEntity;
import com.parqour.bot.entity.IssueCommentEntity;
import com.parqour.bot.entity.TicketEntity;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface TicketService {
    void save(TicketEntity ticket);

    // Youtrack
    List<IssueCommentEntity> saveAllComments(Set<IssueCommentEntity> savingComments);
    List<IssueCommentEntity> saveAllComments(List<IssueComment> issueComments, TicketEntity ticketEntity);

    // Asana
    List<AsanaIssueEntity> saveAllAsanaComments(Set<AsanaIssueEntity> savingComments);
    List<AsanaIssueEntity> saveAllAsanaComments(List<AsanaIssueComment> issueComments, TicketEntity ticketEntity);

    TicketEntity findByAsanaIssueId(String asanaIssueId);

    TicketEntity saveTicket(Ticket ticket);
    Optional<TicketEntity> getTicket(Long ticketId);
    Optional<TicketEntity> getTicketByOrderId(String orderId);

    List<TicketEntity> getTicketsForUpdate();

    List<TicketEntity> getAsanaTicketsForUpdate();

    List<TicketEntity> getAsanaTicketsForUpdateComments();

    void saveComment(IssueCommentEntity comment);

    void saveAll(Set<TicketEntity> updatingTickets);

    void saveAsanaTicket(TicketEntity ticket);
}

