package com.parqour.bot.entity;

import com.parqour.bot.Singleton.Author;
import lombok.*;

import javax.persistence.*;

@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "comments", uniqueConstraints = {@UniqueConstraint(columnNames = "issueCommentId")})
public class IssueCommentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "comments_id_seq")
    @SequenceGenerator(name = "comments_id_seq", initialValue = 10000, allocationSize = 1)
    private Long id;
    private String text;
    private boolean deleted;

    private String issueId;
    private String authorLogin;
    private String authorName;
    private String authorId;

    private Long created;

    private Long updated;
    private String commentId;
    private String issueCommentId;

    @Column(columnDefinition="BOOLEAN DEFAULT false")
    private boolean isCommentSent;

    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "ticket_id")
    private TicketEntity ticket;
}
