package com.parqour.bot.Singleton;

import com.parqour.bot.Enums.State;
import com.parqour.bot.entity.UserEntity;
import lombok.*;

@ToString
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CurrentUser {
    private String chatId;
    private State state;
    private UserEntity user;
    private Ticket ticket;
    private String addingUserPhoneNumber;
    private String addingGroupName;
    private Long addingOperatorId;
    private Long addingDutyOperatorId;
    private Long managingUserId;
    private Long managingParkingId;
}
