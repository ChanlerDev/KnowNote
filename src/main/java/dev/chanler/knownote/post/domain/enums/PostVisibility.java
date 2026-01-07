package dev.chanler.knownote.post.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 帖子可见性
 */
@Getter
@AllArgsConstructor
public enum PostVisibility {
    PUBLIC("public"),
    PRIVATE("private");

    private final String visibility;
}
