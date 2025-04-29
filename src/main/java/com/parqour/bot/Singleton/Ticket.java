package com.parqour.bot.Singleton;

import com.parqour.bot.Enums.CriticalityLevel;
import com.parqour.bot.entity.ParkingEntity;
import com.parqour.bot.entity.UserEntity;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Data
@ToString
@Getter
@Setter
public class Ticket {
    private UserEntity user;
    private ParkingEntity parkingEntity;
    private String project;
    private Integer messageId;
    private String youTrackIssueId;
    private String orderId;
    private String parking;
    private String problemArea;
    private CriticalityLevel criticalityLevel;
    private String summary;
    private String description;
    private String ticketUrl;
    private String asanaTicketUrl;

    public Ticket() {
        this.criticalityLevel = CriticalityLevel.INCIDENT;
    }
}

