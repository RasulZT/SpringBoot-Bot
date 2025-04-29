package com.parqour.bot.entity;

import com.parqour.bot.Enums.Role;
import lombok.*;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "users", uniqueConstraints = {@UniqueConstraint(columnNames = "chatId")})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)  // Ensure only included fields are part of equals and hashCode
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include  // Use id in hashCode and equals
    private Long id;

    @EqualsAndHashCode.Include  // Include username
    private String username;

    private String name;
    private String surname;

    @Enumerated(value = EnumType.STRING)
    private Role role;

    @Column(columnDefinition = "BOOLEAN DEFAULT false")
    private boolean isAdmin;

    @Column(unique = true)
    @EqualsAndHashCode.Include  // Include chatId in hashCode and equals
    private String chatId;

    private String phoneNumber;

    @ManyToMany(fetch = FetchType.EAGER)
    @Column(name = "parking_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude  // Exclude parkings from hashCode and equals
    private Set<ParkingEntity> parkings;

    @ManyToMany(fetch = FetchType.EAGER)
    @Column(name = "user_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude  // Exclude operators from hashCode and equals
    private Set<UserEntity> operators;

    @Column(columnDefinition = "varchar default 'ru'")
    @Enumerated(value = EnumType.STRING)
    private LanguageCode languageCode = LanguageCode.ru;

    public enum LanguageCode {
        en, ru
    }
}
