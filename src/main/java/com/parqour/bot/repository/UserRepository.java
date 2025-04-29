package com.parqour.bot.repository;

import com.parqour.bot.Enums.ProblemArea;
import com.parqour.bot.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByUsername(String username);
    Optional<UserEntity> findByChatId(String chatId);
    Optional<UserEntity> findByPhoneNumber(String phoneNumber);
}
