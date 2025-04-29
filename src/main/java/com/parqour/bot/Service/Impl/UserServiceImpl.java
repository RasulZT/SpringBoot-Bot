package com.parqour.bot.Service.Impl;

import com.parqour.bot.Service.UserService;
import com.parqour.bot.repository.UserRepository;
import com.parqour.bot.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public List<UserEntity> getAlLUsers(){
        return userRepository.findAll();
    }

    @Override
    public UserEntity getUserById(Long userId){
        return userRepository.findById(userId).orElse(null);
    }

    @Override
    public UserEntity getUserByChatId(String chatId) {
        return userRepository.findByChatId(chatId).orElse(null);
    }

    @Override
    public boolean isUserExistByChatId(String chatId){
        return userRepository.findByChatId(chatId).isPresent();
    }

    @Override
    public boolean isUserExistByPhoneNumber(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber).isPresent();
    }

    @Override
    public boolean isUserAdminByChatId(String chatId) {
        UserEntity user = userRepository.findByChatId(chatId).orElse(null);
        if (user == null){
            return false;
        }
        return user.isAdmin();
    }

    @Override
    public StringBuilder getUsersInfoByUsers() {
        List<UserEntity> userEntities = getAlLUsers();
        StringBuilder result = new StringBuilder();
        if (userEntities == null || userEntities.isEmpty()) {
            result.append("Список пользователей пуст!");
            return result;
        }
        for (UserEntity userEntity : userEntities) {
            StringBuilder userInfo = createUserInfo(userEntity);
            if (userInfo != null) {
                result.append(userInfo);
                result.append("\n");
            }
        }
        return result;
    }

    @Override
    public StringBuilder getUsersInfoByUsers(Set<UserEntity> users) {
        StringBuilder result = new StringBuilder();
        if (users == null || users.isEmpty()) {
            result.append("Прикрепленных пользователей пока не найдено!");
            return result;
        }
        for (UserEntity userEntity : users) {
            StringBuilder userInfo = createUserInfo(userEntity);
            if (userInfo != null) {
                result.append(userInfo);
                result.append("\n");
            }
        }
        return result;
    }

    @Override
    public void saveByPhoneNumber(String phoneNumber) {
        UserEntity user = new UserEntity();
        user.setPhoneNumber(phoneNumber);
        save(user);
    }

    @Override
    public UserEntity getUserByPhoneNumber(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber).orElse(null);
    }

    @Override
    public void save(UserEntity user) {
        userRepository.save(user);
    }

    private StringBuilder createUserInfo(UserEntity user) {
        try {
//            if (user.getUsername()!=null) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(getValueOrDefault(user.getPhoneNumber())).append(" ")
                        .append(getValueOrDefault(user.getRole().getLocalizedValue("en"))).append(" ")
                        .append("@").append(getValueOrDefault(user.getUsername()));
                return stringBuilder;
//            }else {
//                return null;
//            }
        }catch (Exception e){
            return null;
        }
    }

//    private StringBuilder createUserInfo(UserEntity user) {
//        try {
//            StringBuilder stringBuilder = new StringBuilder();
//            stringBuilder.append("Телеграм: @").append(getValueOrDefault(user.getUsername())).append("\n");
//            stringBuilder.append("Номер телефона: ").append(getValueOrDefault(user.getPhoneNumber())).append("\n");
//            stringBuilder.append("Роль: ").append(getValueOrDefault(user.getRole().getRussianValue())).append("\n");
//            return stringBuilder;
//        }catch (Exception e){
//            return null;
//        }
//    }

    private String getValueOrDefault(String value) {
        return value != null ? value : "Не указано";
    }

    private boolean isUserExist(String userName){
        return userRepository.findByUsername(userName).isPresent();
    }

    private UserEntity getUserExistByChatId(String chatId){
        return userRepository.findByChatId(chatId).orElse(null);
    }
}
