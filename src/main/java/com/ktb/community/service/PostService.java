package com.ktb.community.service;

import com.ktb.community.dto.likes.LikeResponseDto;
import com.ktb.community.dto.post.PostCreateResponseDto;
import com.ktb.community.dto.post.PostDetailResponseDto;
import com.ktb.community.dto.post.PostListResponseDto;
import com.ktb.community.dto.post.PostRequestDto;
import com.ktb.community.dto.post.PostUpdateResponseDto;
import com.ktb.community.dto.report.ReportRequestDto;
import com.ktb.community.dto.report.ReportResponseDto;
import com.ktb.community.entity.Post;
import com.ktb.community.entity.PostEditHistory;
import com.ktb.community.entity.PostLike;
import com.ktb.community.entity.PostReport;
import com.ktb.community.entity.PostView;
import com.ktb.community.entity.User;
import com.ktb.community.exception.ApiException;
import com.ktb.community.repository.PostEditHistoryRepository;
import com.ktb.community.repository.PostLikeRepository;
import com.ktb.community.repository.PostReportRepository;
import com.ktb.community.repository.PostRepository;
import com.ktb.community.repository.PostViewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PostService {

    private static final long POST_LIMIT_MINUTES = 1L;
    private static final long MAX_POSTS_PER_MINUTE = 3L;
    private static final long ONE_DAY_HOURS = 24L;
    private static final long BLIND_REPORT_THRESHOLD = 5L;

    private final PostRepository postRepository;
    private final PostEditHistoryRepository postEditHistoryRepository;
    private final PostViewRepository postViewRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostReportRepository postReportRepository;

    public PostCreateResponseDto createPost(User user, PostRequestDto request) {
        LocalDateTime oneMinuteAgo = LocalDateTime.now().minusMinutes(POST_LIMIT_MINUTES);

        long recentPostCount = postRepository.countByUserAndCreatedAtAfter(user, oneMinuteAgo);

        if (recentPostCount >= MAX_POSTS_PER_MINUTE) {
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "too_many_requests");
        }

        Post post = new Post(
                user,
                request.getTitle(),
                request.getPostBody(),
                request.getPostImage()
        );

        Post savedPost = postRepository.save(post);

        return new PostCreateResponseDto(
                savedPost.getPostId(),
                savedPost.getTitle(),
                savedPost.getPostBody(),
                savedPost.getPostImage(),
                savedPost.getUser().getUserId(),
                savedPost.getUser().getNickname(),
                savedPost.getUser().getProfileImage(),
                savedPost.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public List<PostListResponseDto> getPostList() {
        return postRepository.findByDeletedFalse()
                .stream()
                .map(post -> {
                    String nickname = post.getUser().isDeleted()
                            ? "unknown_user"
                            : post.getUser().getNickname();

                    String profileImage = post.getUser().isDeleted()
                            ? null
                            : post.getUser().getProfileImage();

                    return new PostListResponseDto(
                            post.getPostId(),
                            post.getTitle(),
                            post.getUser().getUserId(),
                            nickname,
                            profileImage,
                            post.getCreatedAt(),
                            post.getLikes(),
                            post.getComments(),
                            post.getViews(),
                            post.isBlinded()
                    );
                })
                .toList();
    }

    public PostDetailResponseDto getPostDetail(User user, Long postId) {
        Post post = getPost(postId);

        if (post.isDeleted()) {
            throw new ApiException(HttpStatus.NOT_FOUND, "content_not_found");
        }

        boolean isViewCounted = increaseViewIfNeeded(user, post);
        boolean isLiked = postLikeRepository.existsByPostAndUser(post, user);

        return new PostDetailResponseDto(
                post,
                isLiked,
                post.isBlinded(),
                isViewCounted
        );
    }

    public PostUpdateResponseDto updatePost(User user, Long postId, PostRequestDto request) {
        Post post = getPost(postId);

        validatePostOwner(user, post);

        if (post.isDeleted()) {
            throw new ApiException(HttpStatus.CONFLICT, "conflicted state");
        }

        boolean sameTitle = post.getTitle().equals(request.getTitle());
        boolean sameBody = post.getPostBody().equals(request.getPostBody());
        boolean sameImage = String.valueOf(post.getPostImage()).equals(String.valueOf(request.getPostImage()));

        if (sameTitle && sameBody && sameImage) {
            throw new ApiException(HttpStatus.CONFLICT, "conflicted state");
        }

        PostEditHistory history = new PostEditHistory(post);
        postEditHistoryRepository.save(history);

        post.update(
                request.getTitle(),
                request.getPostBody(),
                request.getPostImage()
        );

        return new PostUpdateResponseDto(
                post.getPostId(),
                true,
                post.getUpdatedAt()
        );
    }

    public void deletePost(User user, Long postId) {
        Post post = getPost(postId);

        validatePostOwner(user, post);

        if (post.isDeleted()) {
            throw new ApiException(HttpStatus.CONFLICT, "conflicted state");
        }

        post.delete();
    }

    public LikeResponseDto likePost(User user, Long postId) {
        Post post = getActivePost(postId);

        if (postLikeRepository.existsByPostAndUser(post, user)) {
            throw new ApiException(HttpStatus.CONFLICT, "conflicted state");
        }

        PostLike postLike = new PostLike(post, user);
        postLikeRepository.save(postLike);
        post.increaseLikes();

        return new LikeResponseDto(post.getPostId(), true, post.getLikes());
    }

    public LikeResponseDto unlikePost(User user, Long postId) {
        Post post = getActivePost(postId);

        PostLike postLike = postLikeRepository.findByPostAndUser(post, user)
                .orElseThrow(() -> new ApiException(HttpStatus.CONFLICT, "conflicted state"));

        postLikeRepository.delete(postLike);
        post.decreaseLikes();

        return new LikeResponseDto(post.getPostId(), false, post.getLikes());
    }

    public ReportResponseDto reportPost(User user, Long postId, ReportRequestDto request) {
        Post post = getActivePost(postId);

        if (postReportRepository.existsByPostAndUser(post, user)) {
            throw new ApiException(HttpStatus.CONFLICT, "already_reported");
        }

        PostReport postReport = new PostReport(post, user, request.getReason());
        postReportRepository.save(postReport);

        long reportCount = postReportRepository.countByPost(post);
        if (reportCount >= BLIND_REPORT_THRESHOLD && !post.isBlinded()) {
            post.blind();
        }

        return new ReportResponseDto(post.getPostId(), (int) reportCount, post.isBlinded());
    }

    private boolean increaseViewIfNeeded(User user, Post post) {
        LocalDateTime oneDayAgo = LocalDateTime.now().minusHours(ONE_DAY_HOURS);

        return postViewRepository.findByPostAndUser(post, user)
                .map(postView -> {
                    if (postView.getViewedAt().isBefore(oneDayAgo)) {
                        postView.updateViewedAt();
                        post.increaseViews();
                        return true;
                    }

                    return false;
                })
                .orElseGet(() -> {
                    PostView postView = new PostView(post, user);
                    postViewRepository.save(postView);
                    post.increaseViews();
                    return true;
                });
    }

    private Post getPost(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "content_not_found"));
    }

    private Post getActivePost(Long postId) {
        Post post = getPost(postId);

        if (post.isDeleted()) {
            throw new ApiException(HttpStatus.NOT_FOUND, "content_not_found");
        }

        return post;
    }

    private void validatePostOwner(User user, Post post) {
        if (!post.getUser().getUserId().equals(user.getUserId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "denied_access");
        }
    }
}
