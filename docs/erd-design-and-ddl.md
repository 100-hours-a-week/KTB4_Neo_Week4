# Community ERD 설계 근거 및 DDL

## 1. 설계 방향
이 프로젝트의 엔티티는 `User`, `Post`, `Comment`, `Draft`, `PostEditHistory`, `PostLike`, `PostReport`, `PostView`의 8개로 구성된다. 과제 요구사항이 단순 도식화가 아니라 테이블 설계 자체를 평가하는 형태이므로, 엔티티를 그대로 테이블로 옮기는 수준에서 멈추지 않고 아래 기준으로 물리 모델을 정리하는 것이 적절하다.

1. 엔티티별 책임이 분리되어 있는가
2. 서비스 코드에서 사용되는 비즈니스 규칙이 DB 제약조건으로 보강되는가
3. 중복 저장을 줄이면서도 조회 성능을 확보하는가
4. 소프트 삭제, 신고 누적, 대댓글 구조 같은 커뮤니티 도메인 특성이 반영되는가

이 기준으로 보면 본 프로젝트는 다음 4개 영역으로 나뉜다.

- 사용자 영역: `users`
- 게시글 영역: `posts`, `drafts`, `post_edit_history`
- 상호작용 영역: `post_likes`, `post_views`, `post_reports`
- 댓글 영역: `comments`

## 2. 엔티티를 테이블로 해석한 결과

| 엔티티 | 테이블 | 역할 |
| --- | --- | --- |
| `User` | `users` | 회원 계정 및 탈퇴 상태 관리 |
| `Post` | `posts` | 게시글 본문, 작성자, 집계값 관리 |
| `Comment` | `comments` | 댓글/대댓글 구조 관리 |
| `Draft` | `drafts` | 게시글 임시저장본 관리 |
| `PostEditHistory` | `post_edit_history` | 게시글 수정 전 스냅샷 이력 보관 |
| `PostLike` | `post_likes` | 게시글 좋아요 이력 관리 |
| `PostReport` | `post_reports` | 게시글 신고 이력 및 신고 사유 저장 |
| `PostView` | `post_views` | 사용자별 최근 조회 시각 기록 |

## 3. 테이블별 상세 설계 근거

### 3.1 `users`
핵심 책임은 인증의 기준이 되는 계정 식별자 보관이다. 서비스 코드에서 회원가입 시 `email`, `nickname` 중복을 막고 있으므로 DB 레벨에서도 두 컬럼은 `UNIQUE` 제약이 필요하다.

주요 설계 이유:
- `user_id`를 대리키로 둔다. 이메일은 탈퇴 시 변경되므로 기본키로 부적절하다.
- `email`은 로그인 식별자이므로 `NOT NULL + UNIQUE`가 필요하다.
- `nickname`도 중복 닉네임을 허용하지 않으므로 `NOT NULL + UNIQUE`가 필요하다.
- `deleted`, `deleted_at`은 소프트 삭제를 위한 컬럼이다. 게시글/댓글의 작성자 FK를 보존해야 하므로 물리 삭제보다 소프트 삭제가 맞다.
- 탈퇴 시 이메일을 가공된 값으로 바꾸는 로직이 있으므로, 삭제된 사용자도 `users` 행 자체는 남아 있어야 한다.

### 3.2 `posts`
게시글은 커뮤니티의 중심 엔티티다. 작성자 한 명이 여러 글을 작성할 수 있으므로 `users` : `posts`는 1:N 관계다.

주요 설계 이유:
- `user_id` FK는 게시글 작성자를 나타내므로 `NOT NULL`이어야 한다.
- `title`, `post_body`는 게시글의 핵심 데이터이므로 `NOT NULL`이 적절하다.
- `post_body`는 장문이 가능하므로 `TEXT`가 적절하다.
- `likes`, `views`, `comments`는 각각 `post_likes`, `post_views`, `comments`에서 계산 가능한 값이지만, 목록 조회 성능 때문에 집계 컬럼으로 비정규화한 구조다.
- `edited`, `deleted`, `blinded`는 게시 상태를 표현한다.
- `deleted_at`은 소프트 삭제 시점 추적용이므로 `NULL` 허용이 맞다.
- 게시글 목록은 삭제 여부 필터를 자주 사용하므로 `deleted` 기준 인덱스가 유효하다.

비정규화 근거:
- 좋아요 수, 조회 수, 댓글 수를 매번 집계 테이블에서 `COUNT`하면 목록 조회 비용이 커진다.
- 반면 집계 컬럼을 두면 정합성 위험이 생기므로, 이를 보완하기 위해 `post_likes`, `post_views`, `comments`와 함께 관리한다.

### 3.3 `comments`
댓글은 자기 자신을 참조하는 self-reference 구조가 핵심이다. 현재 서비스 코드는 대댓글의 깊이를 1단계까지만 허용하므로, 구조상 댓글과 대댓글을 같은 테이블에 저장하되 `parent_comment_id`로 계층을 표현하는 방식이 가장 단순하다.

주요 설계 이유:
- `post_id` FK는 댓글이 어느 게시글에 속하는지 나타낸다.
- `user_id` FK는 작성자를 나타낸다.
- `parent_comment_id`가 `NULL`이면 일반 댓글, 값이 있으면 대댓글이다.
- 댓글 본문은 삭제 후에도 행을 남기기 때문에 `deleted` 플래그와 `deleted_at`이 필요하다.
- 댓글 수정 여부를 클라이언트에 알려주므로 `edited` 플래그가 필요하다.

중요한 점:
- "대댓글은 1단계까지만 허용"이라는 규칙은 일반적인 CHECK 제약으로 강제하기 어렵다.
- 따라서 ERD에는 self FK를 두고, 깊이 제한은 서비스 레이어 규칙으로 설명하는 것이 맞다.
- `post_id`, `parent_comment_id` 조합 인덱스가 있으면 게시글별 댓글 트리 조회가 빨라진다.

### 3.4 `drafts`
임시저장 글은 아직 게시되지 않은 작업본을 저장하는 테이블이다. 게시글과 동일한 본문 구조를 가지지만, 게시 여부와 임시저장 시각이라는 별도 상태가 필요하므로 `posts`와 분리하는 편이 맞다.

주요 설계 이유:
- `posts`에 `status = draft`를 두는 방식도 가능하지만, 현재 서비스는 임시저장을 독립 API와 독립 엔티티로 다루므로 별도 테이블이 자연스럽다.
- `published`는 임시저장본이 실제 게시 처리되었는지 추적하는 플래그다.
- 한 사용자가 여러 임시글을 가질 수 있으므로 `users` : `drafts`는 1:N이다.
- 현재 코드의 `updateAt`은 의미상 `updated_at`이 더 적절하므로 ERD/DDL에서는 그렇게 정리하는 편이 좋다.

### 3.5 `post_edit_history`
게시글 수정 이력은 게시글 현재 상태를 덮어쓰기 전에 백업해 두는 스냅샷 테이블이다. 이는 정규화보다 이력 보존이 더 중요한 경우다.

주요 설계 이유:
- 게시글 한 건은 여러 번 수정될 수 있으므로 `posts` : `post_edit_history`는 1:N이다.
- 히스토리에는 수정 당시 제목/본문/이미지를 그대로 복사 저장해야 한다.
- 현재 엔티티는 `postId`만 저장하지만, ERD에서는 `post_id`를 FK로 명시하는 편이 관계가 훨씬 분명하다.
- 원본 게시글이 소프트 삭제되더라도 이력은 유지되어야 하므로 FK 삭제 옵션은 `RESTRICT` 또는 `NO ACTION`이 적절하다.

### 3.6 `post_likes`
좋아요는 사용자와 게시글 사이의 다대다 관계를 푼 교차 엔티티다.

주요 설계 이유:
- 한 사용자는 같은 글에 한 번만 좋아요할 수 있으므로 `UNIQUE (post_id, user_id)`가 핵심이다.
- 이 테이블이 존재해야 `posts.likes` 집계값의 근거 데이터가 생긴다.
- 나중에 좋아요 시각이 필요해질 수 있지만, 현재 프로젝트에는 없으므로 필수 컬럼으로는 두지 않는다.

### 3.7 `post_reports`
신고는 좋아요와 비슷한 교차 엔티티지만, 신고 사유와 신고 시각을 추가로 저장해야 한다.

주요 설계 이유:
- 동일 사용자의 중복 신고를 막기 위해 `UNIQUE (post_id, user_id)`가 필요하다.
- `reason`은 관리자 검토 근거가 되므로 `NOT NULL`이 적절하다.
- 서비스에서 신고 수가 5건 이상이면 게시글을 블라인드 처리하므로, `post_reports`는 단순 로그가 아니라 블라인드 판정의 근거 테이블이다.

### 3.8 `post_views`
조회 이력은 단순 로그라기보다 "사용자별 마지막 조회 시각"을 저장하는 캐시성 테이블에 가깝다.

주요 설계 이유:
- 서비스 로직은 동일 사용자가 같은 글을 24시간 내 다시 보면 조회수를 올리지 않는다.
- 따라서 조회 로그를 무한히 쌓는 구조보다, `(post_id, user_id)`당 한 행만 두고 `viewed_at`을 갱신하는 현재 구조가 목적에 맞다.
- 이 규칙을 위해 `UNIQUE (post_id, user_id)`가 필수다.
- `posts.views`는 이 테이블을 보조 근거로 삼는 비정규화 집계 컬럼이다.

## 4. 관계 설계

### 4.1 핵심 관계
- `users` 1 : N `posts`
- `users` 1 : N `comments`
- `users` 1 : N `drafts`
- `users` 1 : N `post_likes`
- `users` 1 : N `post_reports`
- `users` 1 : N `post_views`
- `posts` 1 : N `comments`
- `posts` 1 : N `post_edit_history`
- `posts` 1 : N `post_likes`
- `posts` 1 : N `post_reports`
- `posts` 1 : N `post_views`
- `comments` 1 : N `comments` (self-reference, parent-comment)

### 4.2 삭제 정책
- `users`, `posts`, `comments`는 소프트 삭제를 사용한다.
- 좋아요/신고/조회/이력은 행 보존 성격이 강하므로 하드 삭제하지 않는 편이 자연스럽다.
- 과제 ERD에서는 FK 삭제 옵션을 `ON DELETE RESTRICT` 또는 기본 `NO ACTION`으로 두는 것이 안전하다.

이유:
- 게시글 작성자가 탈퇴해도 글과 댓글의 작성자 참조는 남아 있어야 한다.
- 게시글이 삭제돼도 신고 이력, 수정 이력, 조회 기준 정보가 남아 있어야 운영상 추적이 가능하다.
- 현재 프로젝트도 실제 삭제 대신 상태값 변경으로 동작한다.

## 5. 정규화와 비정규화 판단

정규화된 테이블:
- `users`, `drafts`, `post_edit_history`, `post_likes`, `post_reports`, `post_views`는 각자의 책임이 분명하다.
- 좋아요/신고/조회 관계를 별도 교차 테이블로 둔 것은 다대다 해소 측면에서 적절하다.

의도적 비정규화:
- `posts.likes`
- `posts.views`
- `posts.comments`

비정규화를 허용한 이유:
- 커뮤니티 메인/목록 화면은 집계 숫자를 매우 자주 읽는다.
- 실시간 `COUNT` 쿼리보다 게시글 테이블에 캐시된 수치를 두는 편이 일반적으로 효율적이다.
- 대신 중복 좋아요 방지, 조회 중복 방지, 댓글 수 증감 로직이 함께 있어야 정합성이 유지된다.

## 6. 인덱스와 제약조건 설계 근거

필수 제약조건:
- `users.email` UNIQUE
- `users.nickname` UNIQUE
- `post_likes(post_id, user_id)` UNIQUE
- `post_reports(post_id, user_id)` UNIQUE
- `post_views(post_id, user_id)` UNIQUE

권장 인덱스:
- `posts(user_id)` : 작성자별 게시글 접근
- `posts(deleted, created_at)` : 삭제 제외 목록 조회
- `comments(post_id, parent_comment_id)` : 게시글별 댓글/대댓글 조회
- `drafts(user_id, published)` : 사용자 임시글 조회 확장 대비
- `post_edit_history(post_id, created_at)` : 게시글 수정 이력 조회

CHECK 제약 권장:
- `posts.likes >= 0`
- `posts.views >= 0`
- `posts.comments >= 0`

## 7. ERDCloud에 그릴 때의 핵심 포인트
ERDCloud에는 아래처럼 표시하면 된다.

1. `users`를 기준 부모 테이블로 둔다.
2. `posts`, `drafts`, `comments`, `post_likes`, `post_reports`, `post_views`를 `users`와 연결한다.
3. `comments.parent_comment_id -> comments.comment_id` self relation을 추가한다.
4. `posts`를 중심으로 `post_edit_history`, `post_likes`, `post_reports`, `post_views`, `comments`를 연결한다.
5. `post_likes`, `post_reports`, `post_views`에는 각각 `(post_id, user_id)` 유니크를 표시한다.
6. `posts`의 `likes`, `views`, `comments`는 집계 캐시 컬럼이라는 점을 회고 문서에 명시한다.

## 8. 제출용 DDL
아래 DDL은 위 설계와 동일한 구조를 만들도록 작성했다. 테이블명과 컬럼명은 과제 제출용 관점에서 `snake_case`로 정리했다.

```sql
CREATE TABLE users (
    user_id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255),
    nickname VARCHAR(100) NOT NULL,
    profile_image VARCHAR(500),
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT uq_users_nickname UNIQUE (nickname)
);

CREATE TABLE posts (
    post_id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    post_body TEXT NOT NULL,
    post_image VARCHAR(500),
    likes INT NOT NULL DEFAULT 0,
    views INT NOT NULL DEFAULT 0,
    comments INT NOT NULL DEFAULT 0,
    edited BOOLEAN NOT NULL DEFAULT FALSE,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    blinded BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_posts_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT ck_posts_likes_nonnegative CHECK (likes >= 0),
    CONSTRAINT ck_posts_views_nonnegative CHECK (views >= 0),
    CONSTRAINT ck_posts_comments_nonnegative CHECK (comments >= 0)
);

CREATE TABLE comments (
    comment_id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    parent_comment_id BIGINT,
    comment_body TEXT NOT NULL,
    edited BOOLEAN NOT NULL DEFAULT FALSE,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_comments_post FOREIGN KEY (post_id) REFERENCES posts(post_id),
    CONSTRAINT fk_comments_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT fk_comments_parent FOREIGN KEY (parent_comment_id) REFERENCES comments(comment_id)
);

CREATE TABLE drafts (
    draft_id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(255),
    post_body TEXT,
    post_image VARCHAR(500),
    published BOOLEAN NOT NULL DEFAULT FALSE,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_drafts_user FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE post_edit_history (
    history_id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    post_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    post_body TEXT NOT NULL,
    post_image VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_post_edit_history_post FOREIGN KEY (post_id) REFERENCES posts(post_id)
);

CREATE TABLE post_likes (
    post_like_id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    user_id BIGINT NOT NULL,
    post_id BIGINT NOT NULL,
    CONSTRAINT fk_post_likes_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT fk_post_likes_post FOREIGN KEY (post_id) REFERENCES posts(post_id),
    CONSTRAINT uq_post_likes_post_user UNIQUE (post_id, user_id)
);

CREATE TABLE post_reports (
    report_id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    reason VARCHAR(1000) NOT NULL,
    reported_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_post_reports_post FOREIGN KEY (post_id) REFERENCES posts(post_id),
    CONSTRAINT fk_post_reports_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT uq_post_reports_post_user UNIQUE (post_id, user_id)
);

CREATE TABLE post_views (
    post_view_id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    viewed_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_post_views_post FOREIGN KEY (post_id) REFERENCES posts(post_id),
    CONSTRAINT fk_post_views_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT uq_post_views_post_user UNIQUE (post_id, user_id)
);

CREATE INDEX idx_posts_user_id ON posts(user_id);
CREATE INDEX idx_posts_deleted_created_at ON posts(deleted, created_at);
CREATE INDEX idx_comments_post_parent ON comments(post_id, parent_comment_id);
CREATE INDEX idx_drafts_user_published ON drafts(user_id, published);
CREATE INDEX idx_post_edit_history_post_created_at ON post_edit_history(post_id, created_at);
```

## 9. 코드 기준으로 설명할 때 강조하면 좋은 부분

### 9.1 왜 `post_likes`, `post_reports`, `post_views`를 별도 테이블로 뒀는가
- 전부 사용자와 게시글 사이의 행위 데이터다.
- 게시글에 컬럼 하나만 두면 "누가" 좋아요/신고/조회했는지 추적할 수 없다.
- 중복 방지 규칙도 테이블 + 유니크 제약으로 가장 명확하게 표현된다.

### 9.2 왜 `posts.likes`, `posts.views`, `posts.comments`를 또 저장하는가
- 메인 리스트와 상세 조회에서 즉시 보여줘야 하는 숫자이기 때문이다.
- 원본 로그 테이블과 집계 캐시 컬럼을 같이 두는 전형적인 커뮤니티 설계다.

### 9.3 왜 `comments`를 자기참조 구조로 설계했는가
- 댓글과 대댓글의 속성이 거의 동일하다.
- 별도 `replies` 테이블을 두면 구조가 과하게 복잡해진다.
- 현재 서비스도 댓글 깊이 제한을 애플리케이션에서 검증하고 있다.

### 9.4 왜 `post_edit_history`를 별도 테이블로 뒀는가
- 현재 게시글은 최신 상태만 보관하면 되지만, 수정 이력은 시점별 스냅샷을 남겨야 한다.
- 이력 테이블을 분리해야 현재 상태 테이블이 비대해지지 않는다.

## 10. 회고 문서에 넣기 좋은 한계와 개선점
- 현재 코드의 `Post.deletedAt`는 생성 시점에 값이 들어가는데, 설계상으로는 삭제 시점에만 값이 있어야 자연스럽다.
- `Draft.updateAt`는 네이밍상 `updated_at`이 더 적절하다.
- `PostEditHistory` 엔티티는 `Long postId`만 두고 있는데, ERD 차원에서는 FK 관계를 명시하는 편이 더 좋다.
- 댓글 2단계 제한은 DB 제약만으로 표현하기 어렵기 때문에 서비스 검증이 병행되어야 한다.
- 추후 확장을 생각하면 `created_at`을 `post_likes`에도 추가할 수 있다.

## 11. 제출물 구성 제안
과제 제출 시 아래 순서로 정리하면 된다.

1. ERDCloud 링크
2. ERD 스냅샷 이미지
3. DDL
4. 테이블 설계 근거 문서
5. 회고
6. AI 사용 기록

이 문서의 3, 4, 10번 내용을 그대로 활용하면 된다.
