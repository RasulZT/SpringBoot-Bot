package com.parqour.bot.Singleton;

import lombok.*;

@Data
@ToString
@Getter
@Setter
@RequiredArgsConstructor
public class IssueComment {
    private String text;
    private boolean deleted;
    private Author author;
    private Long created;

    private Long updated;
    private String id;
}
