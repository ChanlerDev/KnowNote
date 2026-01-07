package dev.chanler.knownote.post.service;

import dev.chanler.knownote.post.api.dto.req.SavePostContentReqDTO;
import dev.chanler.knownote.post.api.dto.req.SavePostMetadataReqDTO;
import dev.chanler.knownote.post.api.dto.resp.CreatePostRespDTO;
import dev.chanler.knownote.post.api.dto.resp.PostRespDTO;
import dev.chanler.knownote.post.api.dto.resp.PostVersionRespDTO;
import dev.chanler.knownote.post.domain.entity.PostDO;

import java.util.List;

/**
 * 帖子服务接口
 */
public interface PostService {

    /**
     * 创建帖子
     */
    CreatePostRespDTO createPost();

    /**
     * 保存帖子内容
     */
    void saveContent(Long postId, SavePostContentReqDTO req);

    /**
     * 保存帖子元数据
     */
    void saveMetadata(Long postId, SavePostMetadataReqDTO req);

    /**
     * 发布帖子
     */
    void publishPost(Long postId);

    /**
     * 获取帖子状态
     */
    PostRespDTO getPost(Long postId);

    /**
     * 删除帖子
     */
    void deletePost(Long postId);

    /**
     * 获取帖子版本历史
     */
    PostVersionRespDTO getVersions(Long postId);
}
