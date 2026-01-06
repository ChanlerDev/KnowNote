package dev.chanler.knownote.user.service;

import dev.chanler.knownote.common.BizException;
import dev.chanler.knownote.common.ErrorCode;
import dev.chanler.knownote.common.PasswordEncoder;
import dev.chanler.knownote.common.UserContext;
import dev.chanler.knownote.user.api.dto.req.GoogleLoginReqDTO;
import dev.chanler.knownote.user.api.dto.req.UpdatePasswordReqDTO;
import dev.chanler.knownote.user.api.dto.req.UpdateProfileReqDTO;
import dev.chanler.knownote.user.api.dto.resp.UserMeRespDTO;
import dev.chanler.knownote.user.domain.entity.UserDO;
import dev.chanler.knownote.user.domain.mapper.UserMapper;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 用户服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Value("${google.client-id:}")
    private String googleClientId;

    private static final String GOOGLE_TOKEN_INFO_URL = "https://oauth2.googleapis.com/tokeninfo?id_token=";

    public UserDO getById(Long id) {
        return userMapper.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.CLIENT_ERROR, "用户不存在"));
    }

    public UserDO getByUsername(String username) {
        return userMapper.findByUsername(username)
                .orElseThrow(() -> new BizException(ErrorCode.CLIENT_ERROR, "用户不存在"));
    }

    /**
     * 获取当前用户信息
     */
    public UserMeRespDTO getMe() {
        Long userId = UserContext.getUserId();
        UserDO user = getById(userId);

        return UserMeRespDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .bio(user.getBio())
                .hasPassword(user.getPasswordHash() != null)
                .hasGoogle(user.getGoogleId() != null)
                .createdAt(user.getCreatedAt())
                .build();
    }

    /**
     * 更新个人资料
     */
    public void updateProfile(UpdateProfileReqDTO req) {
        Long userId = UserContext.getUserId();
        UserDO user = getById(userId);

        if (req.getNickname() != null) {
            user.setNickname(req.getNickname());
        }
        if (req.getAvatar() != null) {
            user.setAvatar(req.getAvatar());
        }
        if (req.getBio() != null) {
            user.setBio(req.getBio());
        }

        user.setUpdatedAt(LocalDateTime.now());
        userMapper.update(user);
    }

    /**
     * 修改密码
     */
    public void updatePassword(UpdatePasswordReqDTO req) {
        Long userId = UserContext.getUserId();
        UserDO user = getById(userId);

        // 已有密码需验证原密码
        if (user.getPasswordHash() != null) {
            if (StrUtil.isBlank(req.getOldPassword())) {
                throw new BizException(ErrorCode.CLIENT_ERROR, "请输入原密码");
            }
            if (!passwordEncoder.matches(req.getOldPassword(), user.getPasswordHash())) {
                throw new BizException(ErrorCode.CLIENT_ERROR, "原密码错误");
            }
        }

        user.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.update(user);
    }

    /**
     * 绑定 Google 账号
     */
    public void bindGoogle(GoogleLoginReqDTO req) {
        Long userId = UserContext.getUserId();
        UserDO user = getById(userId);

        if (user.getGoogleId() != null) {
            throw new BizException(ErrorCode.CLIENT_ERROR, "当前账号已绑定 Google");
        }

        GoogleUserInfo googleUser = verifyGoogleToken(req.getIdToken());

        if (userMapper.findByGoogleId(googleUser.googleId()).isPresent()) {
            throw new BizException(ErrorCode.CLIENT_ERROR, "该 Google 账号已绑定其他用户");
        }

        user.setGoogleId(googleUser.googleId());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.update(user);
    }

    private GoogleUserInfo verifyGoogleToken(String idToken) {
        try {
            String response = HttpUtil.get(GOOGLE_TOKEN_INFO_URL + idToken);
            var json = JSONUtil.parseObj(response);

            String aud = json.getStr("aud");
            if (StrUtil.isNotBlank(googleClientId) && !googleClientId.equals(aud)) {
                throw new BizException(ErrorCode.CLIENT_ERROR, "Google 认证失败");
            }

            return new GoogleUserInfo(
                    json.getStr("sub"),
                    json.getStr("email"),
                    json.getStr("name")
            );
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("Google token 验证失败", e);
            throw new BizException(ErrorCode.THIRD_PARTY_ERROR, "Google 认证失败");
        }
    }

    private record GoogleUserInfo(String googleId, String email, String name) {}
}
