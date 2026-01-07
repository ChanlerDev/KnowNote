package dev.chanler.knownote.storage.service;

import cn.hutool.core.util.StrUtil;
import dev.chanler.knownote.common.BizException;
import dev.chanler.knownote.common.ErrorCode;
import dev.chanler.knownote.config.OssProperties;
import dev.chanler.knownote.storage.api.dto.req.UploadUrlReqDTO;
import dev.chanler.knownote.storage.api.dto.resp.UploadUrlRespDTO;
import dev.chanler.knownote.storage.domain.enums.UploadScene;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

/**
 * 存储服务
 */
@Service
@RequiredArgsConstructor
public class StorageService {

    private final OssProperties ossProperties;
    private final S3Presigner s3Presigner;
    private final S3Client s3Client;

    public UploadUrlRespDTO getUploadUrl(UploadUrlReqDTO req) {
        UploadScene uploadScene = UploadScene.fromScene(req.getScene());

        if (uploadScene != UploadScene.USER_AVATAR) {
            // TODO: 校验资源 ID 是否属于当前用户
        }

        String path = uploadScene.getPath(req.getResourceId(), req.getExt());

        try {
            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10))
                .putObjectRequest(por -> por.bucket(ossProperties.getBucket()).key(path))
                .build();
            PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
            String url = presignedRequest.url().toString();
            return UploadUrlRespDTO.builder()
                .uploadUrl(url)
                .accessUrl(StrUtil.format("{}/{}/{}", ossProperties.getEndpoint(), ossProperties.getBucket(), path))
                .build();
        } catch (Exception e) {
            throw new BizException(ErrorCode.THIRD_PARTY_ERROR, "生成预签名 URL 失败");
        }
    }

    /**
     * 列出指定前缀下的所有对象
     */
    public List<S3Object> listObjects(String prefix) {
        try {
            ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(ossProperties.getBucket())
                .prefix(prefix)
                .build();
            ListObjectsV2Response response = s3Client.listObjectsV2(request);
            return response.contents();
        } catch (Exception e) {
            throw new BizException(ErrorCode.THIRD_PARTY_ERROR, "查询对象列表失败");
        }
    }

    /**
     * 构建公开访问 URL
     */
    public String buildAccessUrl(String key) {
        return StrUtil.format("{}/{}/{}", ossProperties.getEndpoint(), ossProperties.getBucket(), key);
    }
}
