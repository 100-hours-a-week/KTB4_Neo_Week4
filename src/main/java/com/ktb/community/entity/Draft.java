package com.ktb.community.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "drafts")
public class Draft {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long draftId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
    private User user;

    String title;

    @Column(columnDefinition = "TEXT")
    private String postBody;

    private String postImage;

    private boolean published = false;

    private LocalDateTime updateAt;

    public Draft(User user, String title, String postBody, String postImage) {
        this.user = user;
        this.title = title;
        this.postBody = postBody;
        this.postImage = postImage;
        this.updateAt = LocalDateTime.now();
    }

    public void autosave(String title, String postBody, String postImage) {
        this.title = title;
        this.postBody = postBody;
        this.postImage = postImage;
        this.updateAt = LocalDateTime.now();
    }

    public void publish() {
        this.published = true;
    }
}
