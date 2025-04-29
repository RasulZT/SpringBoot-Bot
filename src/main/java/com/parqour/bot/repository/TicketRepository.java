package com.parqour.bot.repository;

import com.parqour.bot.entity.ServiceGroupEntity;
import com.parqour.bot.entity.TicketEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<TicketEntity, Long> {
    TicketEntity findByAsanaIssueId(String asanaIssueId);

    Optional<TicketEntity> getByOrderId(String orderId);

    List<TicketEntity> findAllByYouTrackIssueIdIsNotNullAndIsTicketClosedIsFalse();

    List<TicketEntity> findAllByAsanaIssueIdIsNotNullAndIsTicketClosedIsFalse();

    List<TicketEntity> findAllByAsanaIssueIdIsNotNullAndCommentsUpdatedTimeIsNullOrAsanaIssueIdIsNotNullAndCommentsUpdatedTimeBefore(LocalDateTime oneHourAgo);
}
