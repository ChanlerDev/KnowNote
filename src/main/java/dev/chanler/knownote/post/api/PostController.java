package dev.chanler.knownote.post.api;

import dev.chanler.knownote.common.Result;
import dev.chanler.knownote.post.api.dto.req.SavePostContentReqDTO;
import dev.chanler.knownote.post.api.dto.req.SavePostMetadataReqDTO;
import dev.chanler.knownote.post.api.dto.resp.CreatePostRespDTO;
import dev.chanler.knownote.post.api.dto.resp.PostRespDTO;
import dev.chanler.knownote.post.api.dto.resp.PostVersionRespDTO;
import dev.chanler.knownote.post.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 帖子接口
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/post")
public class PostController {

    private final PostService postService;

    @PostMapping("/create")
    public Result<CreatePostRespDTO> createPost() {
        return Result.ok(postService.createPost());
    }

    @PostMapping("/{postId}/content")
    public Result<Void> savePostContent(@PathVariable Long postId, @Valid @RequestBody SavePostContentReqDTO req) {
        postService.saveContent(postId, req);
        return Result.ok();
    }

    @PostMapping("/{postId}/metadata")
    public Result<Void> savePostMetadata(@PathVariable Long postId, @Valid @RequestBody SavePostMetadataReqDTO req) {
        postService.saveMetadata(postId, req);
        return Result.ok();
    }

    @PostMapping("/{postId}/publish")
    public Result<Void> publishPost(@PathVariable Long postId) {
        postService.publishPost(postId);
        return Result.ok();
    }

    @GetMapping("/{postId}")
    public Result<PostRespDTO> getPost(@PathVariable Long postId) {
        return Result.ok(postService.getPost(postId));
    }

    @PostMapping("/{postId}/delete")
    public Result<Void> deletePost(@PathVariable Long postId) {
        postService.deletePost(postId);
        return Result.ok();
    }

    @GetMapping("/{postId}/versions")
    public Result<PostVersionRespDTO> getVersions(@PathVariable Long postId) {
        return Result.ok(postService.getVersions(postId));
    }
}
