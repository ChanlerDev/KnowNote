package dev.chanler.knownote.post.api.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 保存帖子元数据请求 DTO
 */
@Data
public class SavePostMetadataReqDTO {
    @NotBlank(message = "标题不能为空")
    @Size(max = 255, message = "标题最长255字符")
    private String title;
    @Size(max = 255, message = "描述最长512字符")
    private String description;
    private String tags;
    private String coverUrl;
    @NotBlank(message = "可见性不能为空")
    private String visibility;
    private Integer isTop;
}
