package dev.chanler.knownote.post.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import dev.chanler.knownote.common.BizException;
import dev.chanler.knownote.common.ErrorCode;
import dev.chanler.knownote.common.UserContext;
import dev.chanler.knownote.post.api.dto.req.SavePostContentReqDTO;
import dev.chanler.knownote.post.api.dto.req.SavePostMetadataReqDTO;
import dev.chanler.knownote.post.api.dto.resp.CreatePostRespDTO;
import dev.chanler.knownote.post.api.dto.resp.PostRespDTO;
import dev.chanler.knownote.post.api.dto.resp.PostVersionRespDTO;
import dev.chanler.knownote.post.domain.entity.PostDO;
import dev.chanler.knownote.post.domain.enums.PostStatus;
import dev.chanler.knownote.post.domain.enums.PostType;
import dev.chanler.knownote.post.domain.enums.PostVisibility;
import dev.chanler.knownote.post.domain.mapper.PostMapper;
import dev.chanler.knownote.post.service.PostService;
import dev.chanler.knownote.storage.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;

/**
 * 帖子服务实现类
 */
@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostMapper postMapper;
    private final StorageService storageService;

    @Override
    public CreatePostRespDTO createPost() {
        Long userId = UserContext.getUserId();
        LocalDateTime now = LocalDateTime.now();
        PostDO post = PostDO.builder()
                .id(IdUtil.getSnowflakeNextId())
                .creatorId(userId)
                .status(PostStatus.DRAFT)
                .type(PostType.ARTICLE)
                .visibility(PostVisibility.PRIVATE)
                .isTop(0)
                .createdAt(now)
                .updatedAt(now)
                .build();
        try {
            postMapper.insert(post);
        } catch (Exception e) {
            throw new BizException(ErrorCode.SERVER_ERROR, "创建帖子失败");
        }
        return CreatePostRespDTO.builder().postId(post.getId()).build();
    }

    @Override
    public void saveContent(Long postId, SavePostContentReqDTO req) {
        
    }

    @Override
    public void saveMetadata(Long postId, SavePostMetadataReqDTO req) {

    }

    @Override
    public void publishPost(Long postId) {

    }

    @Override
    public PostRespDTO getPost(Long postId) {
        return null;
    }

    @Override
    public void deletePost(Long postId) {

    }

    @Override
    public PostVersionRespDTO getVersions(Long postId) {
        String prefix = "posts/" + postId + "/content/";
        List<S3Object> objects = storageService.listObjects(prefix);

        List<PostVersionRespDTO.Version> versions = objects.stream()
            .sorted(Comparator.comparing(S3Object::lastModified).reversed())
            .map(obj -> {
                String key = obj.key();
                String filename = key.substring(key.lastIndexOf('/') + 1);
                long timestamp = Long.parseLong(filename.replace(".md", ""));
                return PostVersionRespDTO.Version.builder()
                    .timestamp(timestamp)
                    .url(storageService.buildAccessUrl(key))
                    .createdAt(LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault()))
                    .build();
            })
            .toList();

        return PostVersionRespDTO.builder().versions(versions).build();
    }
}
