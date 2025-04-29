package com.parqour.bot.entity;

import com.parqour.bot.Enums.CriticalityLevel;
import com.parqour.bot.Enums.TicketSection;
import com.parqour.bot.Enums.YouTrackIssueStatus;
import com.parqour.bot.Enums.AsanaIssueStatus;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "tickets", uniqueConstraints = {@UniqueConstraint(columnNames = "orderId")})
public class TicketEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tickets_id_seq")
    @SequenceGenerator(name = "tickets_id_seq", initialValue = 100000, allocationSize = 1)
    private Long id;

    private String orderId;

    private String youTrackIssueId;
    private String asanaIssueId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "parking_id")
    private ParkingEntity parking;

    private String project;
    private String problemArea;
    @Enumerated(EnumType.STRING)
    private CriticalityLevel criticalityLevel;
    @Column(columnDefinition = "TEXT")
    private String summary;
    @Column(columnDefinition = "TEXT")
    private String description;
    private Integer messageId;

    private LocalDateTime commentsUpdatedTime;
    @Column(columnDefinition="BOOLEAN DEFAULT false")
    private boolean isTicketClosed;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar DEFAULT 'CREATED'")
    private YouTrackIssueStatus youTrackIssueStatus = YouTrackIssueStatus.CREATED;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar DEFAULT 'CREATED'")
    private AsanaIssueStatus asanaIssueStatus = AsanaIssueStatus.CREATED;

//    @ToString.Exclude
//    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    private List<IssueCommentEntity> comments;

    @Enumerated(EnumType.STRING)
    @Column(name="section", nullable = false, columnDefinition = "varchar(255) default 'NEW'")
    private TicketSection section = TicketSection.NEW;

    @ToString.Exclude
    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<AsanaIssueEntity> asanaComments;
}
