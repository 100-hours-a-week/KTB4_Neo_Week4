# Current Project ERD Table Spec

현재 프로젝트의 JPA 엔티티 기준으로 ERD에 그리면 되는 테이블은 아래 8개다.

- `users`
- `posts`
- `comments`
- `drafts`
- `postEditHistory`
- `postLikes`
- `postReports`
- `postViews`

아래 명세는 현재 코드 기준으로 작성했다. 즉, 테이블명과 컬럼명은 `camelCase`를 그대로 반영했다.

## 1. users

| 논리명 | 컬럼명 | 타입 | NULL | Default value | 키 | 설명 |
| --- | --- | --- | --- | --- | --- | --- |
| 사용자 ID | `userId` | BIGINT | NOT NULL | Auto Increment | PK | 기본키 |
| 이메일 | `email` | VARCHAR(255) | NOT NULL | 없음 |  | 로그인 식별자 |
| 비밀번호 | `password` | VARCHAR(255) | NULL | `null` |  | 탈퇴 시 `null` 처리 |
| 닉네임 | `nickname` | VARCHAR(255) | NOT NULL | 없음 |  | 사용자 닉네임 |
| 프로필 이미지 | `profileImage` | VARCHAR(500) | NULL | `null` |  | 프로필 이미지 URL |
| 삭제 여부 | `deleted` | BOOLEAN | NOT NULL | `false` |  | 소프트 삭제 플래그 |
| 삭제 일시 | `deletedAt` | TIMESTAMP | NULL | `null` |  | 탈퇴 시점 |

## 2. posts

| 논리명 | 컬럼명 | 타입 | NULL | Default value | 키 | 설명 |
| --- | --- | --- | --- | --- | --- | --- |
| 게시글 ID | `postId` | BIGINT | NOT NULL | Auto Increment | PK | 기본키 |
| 제목 | `title` | VARCHAR(255) | NOT NULL | 없음 |  | 게시글 제목 |
| 본문 | `postBody` | TEXT | NOT NULL | 없음 |  | 게시글 내용 |
| 첨부 이미지 | `postImage` | VARCHAR(500) | NULL | `null` |  | 게시글 이미지 URL |
| 좋아요 수 | `likes` | INT | NOT NULL | `0` |  | 집계 캐시 컬럼 |
| 조회 수 | `views` | INT | NOT NULL | `0` |  | 집계 캐시 컬럼 |
| 댓글 수 | `comments` | INT | NOT NULL | `0` |  | 집계 캐시 컬럼 |
| 수정 여부 | `edited` | BOOLEAN | NOT NULL | `false` |  | 수정 여부 |
| 삭제 여부 | `deleted` | BOOLEAN | NOT NULL | `false` |  | 소프트 삭제 플래그 |
| 블라인드 여부 | `blinded` | BOOLEAN | NOT NULL | `false` |  | 신고 누적 시 블라인드 |
| 생성 일시 | `createdAt` | TIMESTAMP | NOT NULL | 애플리케이션에서 `LocalDateTime.now()` 설정 |  | 생성 시점 |
| 수정 일시 | `updatedAt` | TIMESTAMP | NOT NULL | 애플리케이션에서 `LocalDateTime.now()` 설정 |  | 마지막 수정 시점 |
| 삭제 일시 | `deletedAt` | TIMESTAMP | NULL | `null` |  | 삭제 시점 |
| 사용자 ID | `userId` | BIGINT | NOT NULL | 없음 | FK | `users.userId` |

## 3. comments

| 논리명 | 컬럼명 | 타입 | NULL | Default value | 키 | 설명 |
| --- | --- | --- | --- | --- | --- | --- |
| 댓글 ID | `commentId` | BIGINT | NOT NULL | Auto Increment | PK | 기본키 |
| 댓글 본문 | `commentBody` | TEXT | NOT NULL | 없음 |  | 댓글 내용 |
| 수정 여부 | `edited` | BOOLEAN | NOT NULL | `false` |  | 수정 여부 |
| 삭제 여부 | `deleted` | BOOLEAN | NOT NULL | `false` |  | 소프트 삭제 플래그 |
| 생성 일시 | `createdAt` | TIMESTAMP | NOT NULL | 애플리케이션에서 `LocalDateTime.now()` 설정 |  | 생성 시점 |
| 수정 일시 | `updatedAt` | TIMESTAMP | NOT NULL | 애플리케이션에서 `LocalDateTime.now()` 설정 |  | 마지막 수정 시점 |
| 삭제 일시 | `deletedAt` | TIMESTAMP | NULL | `null` |  | 삭제 시점 |
| 게시글 ID | `postId` | BIGINT | NOT NULL | 없음 | FK | `posts.postId` |
| 사용자 ID | `userId` | BIGINT | NOT NULL | 없음 | FK | `users.userId` |
| 부모 댓글 ID | `parentCommentId` | BIGINT | NULL | `null` | FK | `comments.commentId`, 대댓글용 |

## 4. drafts

| 논리명 | 컬럼명 | 타입 | NULL | Default value | 키 | 설명 |
| --- | --- | --- | --- | --- | --- | --- |
| 임시글 ID | `draftId` | BIGINT | NOT NULL | Auto Increment | PK | 기본키 |
| 제목 | `title` | VARCHAR(255) | NULL | `null` |  | 임시저장 제목 |
| 본문 | `postBody` | TEXT | NULL | `null` |  | 임시저장 본문 |
| 첨부 이미지 | `postImage` | VARCHAR(500) | NULL | `null` |  | 임시저장 이미지 URL |
| 게시 완료 여부 | `published` | BOOLEAN | NOT NULL | `false` |  | 실제 게시 후 `true` |
| 수정 일시 | `updateAt` | TIMESTAMP | NULL | `null` |  | 현재 코드 기준 필드명 유지 |
| 사용자 ID | `userId` | BIGINT | NOT NULL | 없음 | FK | `users.userId` |

## 5. postEditHistory

| 논리명 | 컬럼명 | 타입 | NULL | Default value | 키 | 설명 |
| --- | --- | --- | --- | --- | --- | --- |
| 수정 이력 ID | `historyId` | BIGINT | NOT NULL | Auto Increment | PK | 기본키 |
| 게시글 ID | `postId` | BIGINT | NOT NULL | 없음 | FK | `posts.postId`를 참조하는 이력 대상 |
| 제목 | `title` | VARCHAR(255) | NOT NULL | 없음 |  | 수정 전 제목 스냅샷 |
| 본문 | `postBody` | TEXT | NOT NULL | 없음 |  | 수정 전 본문 스냅샷 |
| 첨부 이미지 | `postImage` | VARCHAR(500) | NULL | `null` |  | 수정 전 이미지 스냅샷 |
| 생성 일시 | `createdAt` | TIMESTAMP | NOT NULL | 애플리케이션에서 `LocalDateTime.now()` 설정 |  | 이력 생성 시점 |

## 6. postLikes

| 논리명 | 컬럼명 | 타입 | NULL | Default value | 키 | 설명 |
| --- | --- | --- | --- | --- | --- | --- |
| 좋아요 ID | `postLikeId` | BIGINT | NOT NULL | Auto Increment | PK | 기본키 |
| 사용자 ID | `userId` | BIGINT | NOT NULL | 없음 | FK | `users.userId` |
| 게시글 ID | `postId` | BIGINT | NOT NULL | 없음 | FK | `posts.postId` |

권장 제약

- `UNIQUE (userId, postId)`

## 7. postReports

| 논리명 | 컬럼명 | 타입 | NULL | Default value | 키 | 설명 |
| --- | --- | --- | --- | --- | --- | --- |
| 신고 ID | `reportId` | BIGINT | NOT NULL | Auto Increment | PK | 기본키 |
| 신고 사유 | `reason` | VARCHAR(1000) | NOT NULL | 없음 |  | 신고 사유 |
| 신고 일시 | `reportedAt` | TIMESTAMP | NOT NULL | 애플리케이션에서 `LocalDateTime.now()` 설정 |  | 신고 시점 |
| 게시글 ID | `postId` | BIGINT | NOT NULL | 없음 | FK | `posts.postId` |
| 사용자 ID | `userId` | BIGINT | NOT NULL | 없음 | FK | `users.userId` |

권장 제약

- `UNIQUE (userId, postId)`

## 8. postViews

| 논리명 | 컬럼명 | 타입 | NULL | Default value | 키 | 설명 |
| --- | --- | --- | --- | --- | --- | --- |
| 조회 ID | `postViewId` | BIGINT | NOT NULL | Auto Increment | PK | 기본키 |
| 조회 일시 | `viewedAt` | TIMESTAMP | NOT NULL | 애플리케이션에서 `LocalDateTime.now()` 설정 |  | 마지막 조회 시점 |
| 게시글 ID | `postId` | BIGINT | NOT NULL | 없음 | FK | `posts.postId` |
| 사용자 ID | `userId` | BIGINT | NOT NULL | 없음 | FK | `users.userId` |

권장 제약

- `UNIQUE (userId, postId)`

## 관계 요약

- `users` 1:N `posts`
- `users` 1:N `comments`
- `users` 1:N `drafts`
- `users` 1:N `postLikes`
- `users` 1:N `postReports`
- `users` 1:N `postViews`
- `posts` 1:N `comments`
- `posts` 1:N `postEditHistory`
- `posts` 1:N `postLikes`
- `posts` 1:N `postReports`
- `posts` 1:N `postViews`
- `comments` 1:N `comments` (`parentCommentId`)

## ERD에 그릴 때 팁

- 이미지 예시처럼 각 테이블 상단에 영문 테이블명만 두고 컬럼은 `camelCase`로 적으면 현재 코드와 가장 잘 맞는다.
- `posts`를 중심 테이블로 두고 `comments`, `postLikes`, `postReports`, `postViews`, `postEditHistory`를 연결하면 된다.
- `comments.parentCommentId -> comments.commentId` 자기참조 관계를 꼭 넣어야 대댓글 구조가 표현된다.
- `postLikes`, `postReports`, `postViews`는 모두 사용자와 게시글 사이의 이력 테이블이다.
