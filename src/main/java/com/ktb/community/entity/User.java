package com.ktb.community.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "users")
public class User {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "userId")
    private Long userId;

    private String email;
    private String password;
    private String nickname;
    private String profileImage;

    private boolean deleted = false;
    private LocalDateTime deletedAt;

    public User(String email, String password, String nickname, String profileImage) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.profileImage = profileImage;
    }

    public void update(String nickname, String profileImage) {
        this.nickname = nickname;
        this.profileImage = profileImage;
    }

    public void updatePassword(String newPassword) {
        this.password = newPassword;
    }

    public void delete() {

        // 유저 탈퇴(soft delete) 후, 탈퇴 유저와 같은 이메일 혹은 닉네임으로
        // 회원 가입하는 경우를 고려하여 적절한 값으로 초기화
        this.email = "deleted-user" + this.userId + "@email.com";
        this.password = null;
        this.nickname = "알 수 없음";
        this.profileImage = null;
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
    }

}
