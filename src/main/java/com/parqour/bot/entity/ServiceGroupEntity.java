package com.parqour.bot.entity;

import lombok.*;

import javax.persistence.*;

@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "service_groups")
public class ServiceGroupEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String groupName;
    private Long groupChatId;
}
