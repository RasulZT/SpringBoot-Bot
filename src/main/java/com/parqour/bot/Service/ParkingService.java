package com.parqour.bot.Service;

import com.parqour.bot.entity.ParkingEntity;
import com.parqour.bot.entity.ServiceGroupEntity;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface ParkingService {
    List<ParkingEntity> getAllParkings();

    Map<Long, ParkingEntity> getAllParkingsAsMap();

    Map<Long, ParkingEntity> getAllParkingsAsMapFromList(Set<ParkingEntity> parkingEntities);

    void deleteAllParkings();

    void saveAll(List<ParkingEntity> parkingEntities);

    void save(ParkingEntity parkingEntity);

    @Transactional
    void updateParkingNames(List<String> parkingNames);

    ParkingEntity getParkingById(Long parkingId);

    Optional<ParkingEntity> getParkingByGroupName(String groupName);

    Optional<ParkingEntity> getParkingByGroupChatId(Long groupChatId);

    void saveGroup(ServiceGroupEntity serviceGroupEntity);

    Optional<ServiceGroupEntity> getGroupByGroupName(String groupName);

    List<ServiceGroupEntity> getAllGroups();

    Optional<ServiceGroupEntity> getGroupByGroupChatId(Long groupChatId);

    Optional<ParkingEntity> getParkingByName(String name);

    StringBuilder getParkingsInfoByUsers(Set<ParkingEntity> parkings);
}
