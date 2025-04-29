package com.parqour.bot.Service.Impl;

import com.parqour.bot.Service.ParkingService;
import com.parqour.bot.entity.ParkingEntity;
import com.parqour.bot.entity.ServiceGroupEntity;
import com.parqour.bot.repository.ParkingRepository;
import com.parqour.bot.repository.ServiceGroupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ParkingServiceImpl implements ParkingService {
    private final ParkingRepository parkingRepository;
    private final ServiceGroupRepository serviceGroupEntityRepository;
    private final String NO_ASSIGNED_PARKING_TEXT = "Прикрепленных парковок пока не найдено!";

    @Override
    public List<ParkingEntity> getAllParkings() {
        return parkingRepository.findAll();
    }

    @Override
    public Map<Long, ParkingEntity> getAllParkingsAsMap() {
        Map<Long, ParkingEntity> parkingsMap = new HashMap<>();
        List<ParkingEntity> parkings = getAllParkings();
        for (ParkingEntity parking : parkings) {
            parkingsMap.put(parking.getId(), parking);
        }
        return parkingsMap;
    }

    @Override
    public Map<Long, ParkingEntity> getAllParkingsAsMapFromList(Set<ParkingEntity> parkingEntities) {
        Map<Long, ParkingEntity> parkingsMap = new HashMap<>();
        for (ParkingEntity parking : parkingEntities) {
            parkingsMap.put(parking.getId(), parking);
        }
        return parkingsMap;
    }

    @Override
    public void deleteAllParkings() {
        parkingRepository.deleteAll();
    }

    @Override
    public void saveAll(List<ParkingEntity> parkingEntities) {
        parkingRepository.saveAll(parkingEntities);
    }

    @Override
    public void save(ParkingEntity parkingEntity) {
        parkingRepository.save(parkingEntity);
    }

    @Override
    @Transactional
    public void updateParkingNames(List<String> parkingNames) {
        parkingRepository.deleteByNameNotIn(parkingNames);

        List<ParkingEntity> existingParkings = parkingRepository.findByNameIn(parkingNames);
        List<String> existingParkingNames = existingParkings.stream()
                .map(ParkingEntity::getName).toList();

        for (String name : parkingNames) {
            if (!existingParkingNames.contains(name)) {
                ParkingEntity parking = new ParkingEntity();
                parking.setName(name);
                parkingRepository.save(parking);
            }
        }
    }

    @Override
    public ParkingEntity getParkingById(Long parkingId) {
        return parkingRepository.findById(parkingId).orElse(null);
    }

    @Override
    public Optional<ParkingEntity> getParkingByGroupName(String groupName) {
        return parkingRepository.getByGroupName(groupName);
    }

    @Override
    public Optional<ParkingEntity> getParkingByGroupChatId(Long groupChatId) {
        return parkingRepository.getByGroupChatId(groupChatId);
    }

    @Override
    public void saveGroup(ServiceGroupEntity serviceGroupEntity) {
        serviceGroupEntityRepository.save(serviceGroupEntity);
    }

    @Override
    public Optional<ServiceGroupEntity> getGroupByGroupName(String groupName) {
        return serviceGroupEntityRepository.getByGroupName(groupName);
    }

    @Override
    public List<ServiceGroupEntity> getAllGroups() {
        return serviceGroupEntityRepository.findAll();
    }

    @Override
    public Optional<ServiceGroupEntity> getGroupByGroupChatId(Long groupChatId) {
        return serviceGroupEntityRepository.getByGroupChatId(groupChatId);
    }

    @Override
    public Optional<ParkingEntity> getParkingByName(String name) {
        return serviceGroupEntityRepository.getByName(name);
    }

    @Override
    public StringBuilder getParkingsInfoByUsers(Set<ParkingEntity> parkings) {
        StringBuilder stringBuilder = new StringBuilder();
        if (!parkings.isEmpty()) {
            for (ParkingEntity parking : parkings) {
                stringBuilder.append(parking.getName()).append(" - ");
                if (parking.getGroupName() != null && !parking.getGroupName().isBlank()) {
                    stringBuilder.append(parking.getGroupName());
                }
            }
        } else {
            stringBuilder.append(NO_ASSIGNED_PARKING_TEXT);
        }
        return stringBuilder;
    }
}
