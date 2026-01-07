package dev.chanler.knownote.post.api.dto.resp;

import dev.chanler.knownote.post.domain.enums.PostStatus;
import dev.chanler.knownote.post.domain.enums.PostType;
import dev.chanler.knownote.post.domain.enums.PostVisibility;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 帖子响应 DTO
 */
@Data
public class PostRespDTO {
    private Long id;
    private Long creatorId;
    private PostStatus status;
    private PostType type;

    private String title;
    private String description;
    private String tags;
    private String coverUrl;
    private PostVisibility visibility;
    private Integer isTop;

    private String contentUrl;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime publishedAt;
}
