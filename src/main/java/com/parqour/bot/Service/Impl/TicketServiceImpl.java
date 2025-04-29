package com.parqour.bot.Service.Impl;

import com.parqour.bot.Enums.YouTrackIssueStatus;
import com.parqour.bot.Service.TicketService;
import com.parqour.bot.Model.AsanaIssueComment;
import com.parqour.bot.Singleton.Author;
import com.parqour.bot.Singleton.IssueComment;
import com.parqour.bot.Singleton.Ticket;
import com.parqour.bot.entity.AsanaIssueEntity;
import com.parqour.bot.entity.IssueCommentEntity;
import com.parqour.bot.entity.TicketEntity;
import com.parqour.bot.repository.AsanaIssueCommentRepository;
import com.parqour.bot.repository.IssueCommentRepository;
import com.parqour.bot.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketServiceImpl implements TicketService {
    private final TicketRepository ticketRepository;
    private final IssueCommentRepository issueCommentRepository;
    private final AsanaIssueCommentRepository asanaIssueCommentRepository;

    @Override
    public void save(TicketEntity ticket) {
        ticketRepository.save(ticket);
    }

    @Override
    public void saveComment(IssueCommentEntity comment) {
        issueCommentRepository.save(comment);
    }

    @Override
    @Transactional
    public void saveAll(Set<TicketEntity> updatingTickets) {
        try {
            ticketRepository.saveAll(updatingTickets);
            log.info("Successfully saved {} updated tickets", updatingTickets.size());
        } catch (Exception e) {
            log.error("Error saving updated tickets", e);
        }
    }

    @Override
    @Transactional
    public void saveAsanaTicket(TicketEntity ticket) {
        try {
            ticketRepository.save(ticket);
            log.info("Successfully saved ticket: {}", ticket);
        } catch (Exception e) {
            log.error("Error while saving asana ticket: {}", e.getMessage());
        }
    }

    @Override
    public List<IssueCommentEntity> saveAllComments(Set<IssueCommentEntity> savingComments) {
        try {
            return issueCommentRepository.saveAll(savingComments);
        } catch (Exception e) {
            log.error("saveAllComments batch error {}", e.getMessage());
            return null;
        }

    }

    @Override
    public List<AsanaIssueEntity> saveAllAsanaComments(Set<AsanaIssueEntity> savingComments) {
        try {
            return asanaIssueCommentRepository.saveAll(savingComments);
        } catch (Exception e) {
            log.error("saveAllComments batch error {}", e.getMessage());
            return null;
        }

    }

    @Override
    public List<IssueCommentEntity> saveAllComments(List<IssueComment> issueComments, TicketEntity ticketEntity) {
        Set<IssueCommentEntity> savingComments = new HashSet<>();

//        HashMap<String, IssueCommentEntity> existingComments = commentEntityHashMap(ticketEntity.getComments());
//        for (IssueComment issueComment : issueComments) {
//
//            if (issueComment.getText() != null && issueComment.getText().contains("@support-bot")) {
//                IssueCommentEntity issueCommentEntity = existingComments.get(issueComment.getId());
//
//                if (issueCommentEntity == null) {
//                    issueCommentEntity = convertCommentToEntity(issueComment, ticketEntity);
//                    if (issueCommentEntity != null) {
//                        savingComments.add(issueCommentEntity);
//                    }
//                }
//
//            }
//        }
//        List<IssueCommentEntity> savedComments = saveAllComments(savingComments);
//        if (savedComments != null && !savedComments.isEmpty()) {
//            return savedComments;
//        } else {
            return null;
        //}
    }

    @Override
    public List<AsanaIssueEntity> saveAllAsanaComments(List<AsanaIssueComment> issueComments, TicketEntity ticketEntity) {
        Set<AsanaIssueEntity> savingComments = new HashSet<>();

        HashMap<String, AsanaIssueEntity> existingComments = commentAsanaEntityHashMap(ticketEntity.getAsanaComments());
        for (AsanaIssueComment issueComment : issueComments) {
            if (issueComment.getText() != null && issueComment.getText().contains("/sb")) {
                AsanaIssueEntity issueCommentEntity = existingComments.get(issueComment.getGid());

                if (issueCommentEntity == null) {
                    issueCommentEntity = convertAsanaCommentToEntity(issueComment, ticketEntity);
                    if (issueCommentEntity != null) {
                        savingComments.add(issueCommentEntity);
                    }
                }

            }
        }
        List<AsanaIssueEntity> savedComments = saveAllAsanaComments(savingComments);
        if (savedComments != null && !savedComments.isEmpty()) {
            return savedComments;
        } else {
            return null;
        }
    }

    @Override
    public TicketEntity findByAsanaIssueId(String asanaIssueId) {
        return ticketRepository.findByAsanaIssueId(asanaIssueId);
    }

    @Override
    public TicketEntity saveTicket(Ticket ticket) {
        try {
            TicketEntity ticketEntity = new TicketEntity();
            ticketEntity.setParking(ticket.getParkingEntity());
            ticketEntity.setProject(ticket.getProject());
            ticketEntity.setOrderId(ticket.getOrderId());
            ticketEntity.setUser(ticket.getUser());
            ticketEntity.setSummary(ticket.getSummary());
            ticketEntity.setDescription(ticket.getDescription());
            ticketEntity.setCriticalityLevel(ticket.getCriticalityLevel());
            ticketEntity.setProblemArea(ticket.getProblemArea());
            ticketEntity.setMessageId(ticket.getMessageId());
            ticketEntity.setYouTrackIssueStatus(YouTrackIssueStatus.CREATED);
            save(ticketEntity);
            return ticketEntity;
        } catch (Exception e) {
            log.error("Не удалось сохранить тикет - " + e.getMessage());
            return null;
        }
    }

    public AsanaIssueEntity convertAsanaCommentToEntity(AsanaIssueComment comment, TicketEntity ticket) {
        try {
            AsanaIssueEntity entity = new AsanaIssueEntity();
            entity.setIssueId(ticket.getAsanaIssueId());
            entity.setText(comment.getText());
            entity.setDeleted(false);

            if (comment.getCreatedBy() != null) {
                Author author = comment.getCreatedBy();
                entity.setAuthorGid(author.getGid());
                entity.setAuthorName(author.getName());
                entity.setAuthorResourceType(author.getResourceType());
            }

            if (comment.getCreatedAt() != null) {
                Instant createdInstant = Instant.parse(comment.getCreatedAt());
                LocalDateTime createdDateTime = LocalDateTime.ofInstant(createdInstant, ZoneId.systemDefault());
                entity.setCreatedAt(createdDateTime);
            } else {
                entity.setCreatedAt(LocalDateTime.now());
            }

            entity.setUpdatedAt(LocalDateTime.now());

            entity.setCommentId(comment.getGid());
            entity.setIssueCommentId(ticket.getAsanaIssueId() + comment.getGid());

            entity.setTicket(ticket);
            return entity;
        } catch (Exception e) {
            log.error("Не удалось сохранить коммент - " + e.getMessage());
            return null;
        }
    }

    @Override
    public Optional<TicketEntity> getTicket(Long ticketId) {
        return ticketRepository.findById(ticketId);
    }

    @Override
    public Optional<TicketEntity> getTicketByOrderId(String orderId) {
        return ticketRepository.getByOrderId(orderId);
    }

    @Override
    public List<TicketEntity> getTicketsForUpdate() {
        return ticketRepository.findAllByYouTrackIssueIdIsNotNullAndIsTicketClosedIsFalse();
    }

    @Override
    public List<TicketEntity> getAsanaTicketsForUpdate() {
        return ticketRepository.findAllByAsanaIssueIdIsNotNullAndIsTicketClosedIsFalse();
    }

    @Override
    public List<TicketEntity> getAsanaTicketsForUpdateComments() {
        LocalDateTime seconds = LocalDateTime.now().minusSeconds(20);
        return ticketRepository.findAllByAsanaIssueIdIsNotNullAndCommentsUpdatedTimeIsNullOrAsanaIssueIdIsNotNullAndCommentsUpdatedTimeBefore(seconds);
    }

    private HashMap<String, IssueCommentEntity> commentEntityHashMap(List<IssueCommentEntity> issueCommentEntities) {
        HashMap<String, IssueCommentEntity> commentEntityHashMap = new HashMap<>();
        if (issueCommentEntities != null) {
            for (IssueCommentEntity issueCommentEntity : issueCommentEntities) {
                commentEntityHashMap.put(issueCommentEntity.getCommentId(), issueCommentEntity);
            }
        }
        return commentEntityHashMap;
    }

    private HashMap<String, AsanaIssueEntity> commentAsanaEntityHashMap(List<AsanaIssueEntity> issueCommentEntities) {
        HashMap<String, AsanaIssueEntity> commentEntityHashMap = new HashMap<>();
        if (issueCommentEntities != null) {
            for (AsanaIssueEntity issueCommentEntity : issueCommentEntities) {
                commentEntityHashMap.put(issueCommentEntity.getCommentId(), issueCommentEntity);
            }
        }
        return commentEntityHashMap;
    }
}

