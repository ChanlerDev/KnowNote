package dev.chanler.knownote.storage.api;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.chanler.knownote.common.Result;
import dev.chanler.knownote.storage.api.dto.req.UploadUrlReqDTO;
import dev.chanler.knownote.storage.api.dto.resp.UploadUrlRespDTO;
import dev.chanler.knownote.storage.service.StorageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 存储接口
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/oss")
public class StorageController {

    private final StorageService storageService;

    @PostMapping("/url")
    public Result<UploadUrlRespDTO> getUploadUrl(@Valid @RequestBody UploadUrlReqDTO req) {
        return Result.ok(storageService.getUploadUrl(req));
    }
}
