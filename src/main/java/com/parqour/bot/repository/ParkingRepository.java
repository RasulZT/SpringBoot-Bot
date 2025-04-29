package com.parqour.bot.repository;

import com.parqour.bot.entity.ParkingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ParkingRepository extends JpaRepository<ParkingEntity, Long> {

    @Query(nativeQuery = true, value = "SELECT DISTINCT(name) from parkings where name LIKE :parkingName LIMIT 10")
    List<String> findAllSimilarParkings(@Param("parkingName") String parkingName);

    void deleteByNameNotIn(List<String> parkingNames);

    List<ParkingEntity> findByNameIn(List<String> parkingNames);

    Optional<ParkingEntity> getByGroupName(String groupName);
    Optional<ParkingEntity> getByGroupChatId(Long groupChatId);
}
