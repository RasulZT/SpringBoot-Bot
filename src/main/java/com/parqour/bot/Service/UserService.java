package com.parqour.bot.Service;

import com.parqour.bot.Enums.ProblemArea;
import com.parqour.bot.entity.UserEntity;

import java.util.List;
import java.util.Set;

public interface UserService {

    List<UserEntity> getAlLUsers();

    UserEntity getUserById(Long userId);
    UserEntity getUserByChatId(String chatId);

    boolean isUserExistByChatId(String chatId);
    boolean isUserExistByPhoneNumber(String phoneNumber);
    boolean isUserAdminByChatId(String chatId);
    StringBuilder getUsersInfoByUsers();

    StringBuilder getUsersInfoByUsers(Set<UserEntity> users);

    void saveByPhoneNumber(String phoneNumber);

    UserEntity getUserByPhoneNumber(String phoneNumber);

    void save(UserEntity user);
}
