package com.ktb.community.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "postReports")
public class PostReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reportId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "postId")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
    private User user;

    private String reason;

    private LocalDateTime reportedAt;

    public PostReport(Post post, User user, String reason) {
        this.post = post;
        this.user = user;
        this.reason = reason;
        this.reportedAt = LocalDateTime.now();
    }
}
