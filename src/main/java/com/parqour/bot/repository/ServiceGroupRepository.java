package com.parqour.bot.repository;

import com.parqour.bot.entity.ParkingEntity;
import com.parqour.bot.entity.ServiceGroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ServiceGroupRepository extends JpaRepository<ServiceGroupEntity, Long> {
    Optional<ServiceGroupEntity> getByGroupName(String groupName);
    Optional<ServiceGroupEntity> getByGroupChatId(Long groupChatId);

    Optional<ParkingEntity> getByName(String name);
}
