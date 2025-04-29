package com.parqour.bot.entity;

import javax.persistence.*;
import lombok.*;


@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "parkings")
public class ParkingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String host;
    private String ip;
    private String googleTableLink;
    private String groupName;
    private Long groupChatId;

    @Column(columnDefinition = "varchar default 'ru'")
    @Enumerated(value = EnumType.STRING)
    private UserEntity.LanguageCode languageCode = UserEntity.LanguageCode.ru;
}
