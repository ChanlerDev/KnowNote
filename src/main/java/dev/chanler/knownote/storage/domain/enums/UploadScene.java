package dev.chanler.knownote.storage.domain.enums;

import dev.chanler.knownote.common.BizException;
import dev.chanler.knownote.common.ErrorCode;
import dev.chanler.knownote.common.UserContext;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

/**
 * oss 场景
 */
@Getter
@AllArgsConstructor
public enum UploadScene {
    USER_AVATAR("user_avatar", "用户头像"),
    POST_COVER("post_cover", "帖子封面"),
    POST_CONTENT("post_content", "帖子正文"),
    POST_IMAGE("post_image", "内嵌图片");

    private final String scene;
    private final String description;

    /**
     * 根据场景字符串获取枚举
     */
    public static UploadScene fromScene(String scene) {
        for (UploadScene uploadScene : UploadScene.values()) {
            if (uploadScene.getScene().equals(scene)) {
                return uploadScene;
            }
        }
        throw new BizException(ErrorCode.CLIENT_ERROR, "不支持的上传场景：" + scene);
    }

    /**
     * 获取存储路径
     * avatar: users/{userId}/avatar/{timestamp}.{ext}
     * post cover: posts/{postId}/cover/{timestamp}.{ext}
     * post content: posts/{postId}/content/{timestamp}.md
     * post image: posts/{postId}/images/{timestamp}.{ext}
     */
    public String getPath(String resourceId, String ext) {
        return switch (this) {
            case USER_AVATAR -> "users/" + UserContext.getUserId() + "/avatar/" + Instant.now().toEpochMilli() + "." + ext;
            case POST_COVER -> "posts/" + resourceId + "/cover/" + Instant.now().toEpochMilli() + "." + ext;
            case POST_CONTENT -> "posts/" + resourceId + "/content/" + Instant.now().toEpochMilli() + ".md";
            case POST_IMAGE -> "posts/" + resourceId + "/images/" + Instant.now().toEpochMilli() + "." + ext;
        };
    }
}
