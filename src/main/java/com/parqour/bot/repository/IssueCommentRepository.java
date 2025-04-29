package com.parqour.bot.repository;

import com.parqour.bot.entity.IssueCommentEntity;
import com.parqour.bot.entity.TicketEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IssueCommentRepository extends JpaRepository<IssueCommentEntity, Long> {
    List<IssueCommentEntity> findAllByTicketId(Long ticketId);
}
