package com.parqour.bot.repository;

import com.parqour.bot.entity.DutiesEntity;
import com.parqour.bot.entity.TicketEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DutyRepository extends JpaRepository<DutiesEntity, Long> {
    List<DutiesEntity> getAllByDutyDateEquals(LocalDate date);
    List<DutiesEntity> findAllByDutyDateEquals(LocalDate date);
}
