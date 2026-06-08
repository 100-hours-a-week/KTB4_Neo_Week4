package com.ktb.community.dto.post;

import com.ktb.community.entity.Post;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PostDetailResponseDto {

    private PostInfo post;
    private AuthorInfo author;
    private MetaInfo meta;

    public PostDetailResponseDto(Post post, boolean isLiked, boolean isBlinded, boolean isViewCounted) {
        this.post = new PostInfo(post);
        this.author = new AuthorInfo(post);
        this.meta = new MetaInfo(post, isLiked, post.isBlinded(), isViewCounted);
    }

    @Getter
    public static class PostInfo {

        private Long postId;
        private String title;
        private String postBody;
        private String postImage;
        private LocalDateTime createdAt;

        public PostInfo(Post post) {
            this.postId = post.getPostId();
            this.title = post.getTitle();
            this.postBody = post.getPostBody();
            this.postImage = post.getPostImage();
            this.createdAt = post.getCreatedAt();
        }
    }

    @Getter
    public static class AuthorInfo {
        private Long userId;
        private String nickname;
        private String profileImage;

        public AuthorInfo(Post post) {
            this.userId = post.getUser().getUserId();
            this.nickname = post.getUser().getNickname();
            this.profileImage = post.getUser().getProfileImage();
        }
    }

    @Getter
    public static class MetaInfo {
        private int likes;
        private int views;
        private int comments;
        private boolean isLiked;
        private boolean isViewCounted;
        private boolean isBlinded;

        public MetaInfo(Post post, boolean isLiked, boolean isViewCounted, boolean isBlinded) {
            this.likes = post.getLikes();
            this.views = post.getViews();
            this.comments = post.getComments();
            this.isLiked = isLiked;
            this.isViewCounted = isViewCounted;
            this.isBlinded = isBlinded;
        }
    }
}
