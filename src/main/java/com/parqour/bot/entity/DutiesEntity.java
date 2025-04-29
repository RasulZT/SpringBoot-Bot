package com.parqour.bot.entity;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;

@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "duties")
public class DutiesEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    private LocalDate dutyDate;

    @ManyToOne
    @JoinColumn(name = "assigned_user_id")
    private UserEntity assignedUser;
}