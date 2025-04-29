package com.parqour.bot.repository;

import com.parqour.bot.entity.AsanaIssueEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AsanaIssueCommentRepository extends JpaRepository<AsanaIssueEntity, Long> {
}
