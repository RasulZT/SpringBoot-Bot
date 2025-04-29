package com.parqour.bot.entity;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "asanaComments", uniqueConstraints = {@UniqueConstraint(columnNames = "issueCommentId")})
public class AsanaIssueEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "comments_id_seq")
    @SequenceGenerator(name = "comments_id_seq", initialValue = 10000, allocationSize = 1)
    private Long id;

    private String text;
    private boolean deleted;

    private String issueId;
    private String authorResourceType;
    private String authorName;
    private String authorGid;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    private String commentId;
    private String issueCommentId;

    @Column(columnDefinition="BOOLEAN DEFAULT false")
    private boolean isCommentSent;

    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "ticket_id")
    private TicketEntity ticket;
}
