package dev.chanler.knownote.user.service;

import dev.chanler.knownote.common.BizException;
import dev.chanler.knownote.common.ErrorCode;
import dev.chanler.knownote.common.PasswordEncoder;
import dev.chanler.knownote.common.UserContext;
import dev.chanler.knownote.user.api.dto.req.*;
import dev.chanler.knownote.user.api.dto.resp.TokenRespDTO;
import dev.chanler.knownote.user.domain.entity.UserDO;
import dev.chanler.knownote.user.domain.mapper.UserMapper;

import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 认证服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final VerifyCodeService verifyCodeService;
    private final TokenService tokenService;

    @Value("${google.client-id:}")
    private String googleClientId;

    private static final String AVATAR_TEMPLATE = "https://api.dicebear.com/9.x/pixel-art/svg?seed={}";
    private static final String GOOGLE_TOKEN_INFO_URL = "https://oauth2.googleapis.com/tokeninfo?id_token=";

    /**
     * 发送验证码
     */
    public void sendCode(SendCodeReqDTO req) {
        verifyCodeService.sendCode(req.getEmail());
    }

    /**
     * 邮箱注册
     */
    @Transactional
    public TokenRespDTO register(RegisterReqDTO req) {
        // 校验验证码
        if (!verifyCodeService.verifyCode(req.getEmail(), req.getCode())) {
            throw new BizException(ErrorCode.CLIENT_ERROR, "验证码错误或已过期");
        }

        // 检查邮箱唯一性
        if (userMapper.findByEmail(req.getEmail()).isPresent()) {
            throw new BizException(ErrorCode.CLIENT_ERROR, "邮箱已被注册");
        }

        // 检查用户名唯一性
        if (userMapper.findByUsername(req.getUsername()).isPresent()) {
            throw new BizException(ErrorCode.CLIENT_ERROR, "用户名已被使用");
        }

        // 创建用户
        LocalDateTime now = LocalDateTime.now();
        UserDO user = UserDO.builder()
                .email(req.getEmail())
                .username(req.getUsername())
                .nickname(req.getNickname())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .avatar(StrUtil.format(AVATAR_TEMPLATE, req.getUsername()))
                .createdAt(now)
                .updatedAt(now)
                .build();

        userMapper.insert(user);
        verifyCodeService.consumeCode(req.getEmail());

        // TODO: deviceInfo 待获取
        return tokenService.issueTokenPair(user.getId(), null);
    }

    /**
     * 密码登录
     */
    public TokenRespDTO loginByPassword(PasswordLoginReqDTO req) {
        // 根据账号查找用户（支持邮箱或用户名）
        Optional<UserDO> userOpt = Validator.isEmail(req.getAccount())
                ? userMapper.findByEmail(req.getAccount())
                : userMapper.findByUsername(req.getAccount());

        UserDO user = userOpt.orElseThrow(() ->
                new BizException(ErrorCode.CLIENT_ERROR, "账号或密码错误"));

        // 检查密码
        if (user.getPasswordHash() == null) {
            throw new BizException(ErrorCode.CLIENT_ERROR, "该账号未设置密码，请使用验证码或 Google 登录");
        }

        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new BizException(ErrorCode.CLIENT_ERROR, "账号或密码错误");
        }

        // TODO: deviceInfo 待获取
        return tokenService.issueTokenPair(user.getId(), null);
    }

    /**
     * 验证码登录
     */
    public TokenRespDTO loginByCode(CodeLoginReqDTO req) {
        // 校验验证码
        if (!verifyCodeService.verifyCode(req.getEmail(), req.getCode())) {
            throw new BizException(ErrorCode.CLIENT_ERROR, "验证码错误或已过期");
        }

        // 查找用户
        UserDO user = userMapper.findByEmail(req.getEmail())
                .orElseThrow(() -> new BizException(ErrorCode.CLIENT_ERROR, "该邮箱未注册"));

        verifyCodeService.consumeCode(req.getEmail());
        return tokenService.issueTokenPair(user.getId(), null);
    }

    /**
     * Google 登录
     */
    @Transactional
    public TokenRespDTO loginByGoogle(GoogleLoginReqDTO req) {
        // 验证 Google ID Token
        GoogleUserInfo googleUser = verifyGoogleToken(req.getIdToken());

        // 1. 先用 googleId 查找
        Optional<UserDO> userByGoogleId = userMapper.findByGoogleId(googleUser.googleId());
        if (userByGoogleId.isPresent()) {
            // TODO: deviceInfo 待获取
            return tokenService.issueTokenPair(userByGoogleId.get().getId(), null);
        }

        // 2. 用 email 查找，找到则绑定 googleId
        Optional<UserDO> userByEmail = userMapper.findByEmail(googleUser.email());
        if (userByEmail.isPresent()) {
            UserDO user = userByEmail.get();
            user.setGoogleId(googleUser.googleId());
            user.setUpdatedAt(LocalDateTime.now());
            userMapper.update(user);
            // TODO: deviceInfo 待获取
            return tokenService.issueTokenPair(user.getId(), null);
        }

        // 3. 都没有，创建新用户
        String nickname = StrUtil.blankToDefault(googleUser.name(), null);
        LocalDateTime now = LocalDateTime.now();

        int attempts = 0;
        while (attempts < 10) {
            String randomUsername = "user_" + RandomUtil.randomString(8);
            String finalNickname = StrUtil.blankToDefault(nickname, randomUsername);

            UserDO user = UserDO.builder()
                    .email(googleUser.email())
                    .username(randomUsername)
                    .nickname(finalNickname)
                    .googleId(googleUser.googleId())
                    .avatar(StrUtil.format(AVATAR_TEMPLATE, randomUsername))
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            try {
                userMapper.insert(user);
                // TODO: deviceInfo 待获取
                return tokenService.issueTokenPair(user.getId(), null);
            } catch (DuplicateKeyException e) {
                attempts++;
                log.warn("用户名冲突，重试第{}次: {}", attempts, randomUsername);
            }
        }

        throw new BizException(ErrorCode.SERVER_ERROR, "生成用户名失败，请重试");
    }

    /**
     * 刷新 Token
     */
    public TokenRespDTO refresh(RefreshReqDTO req) {
        return tokenService.refresh(req.getRefreshToken());
    }

    /**
     * 登出
     */
    public void logout(LogoutReqDTO req) {
        Long userId = UserContext.getUserId();
        tokenService.logout(userId, req.getRefreshToken());
    }

    private GoogleUserInfo verifyGoogleToken(String idToken) {
        try {
            String response = HttpUtil.get(GOOGLE_TOKEN_INFO_URL + idToken);
            var json = JSONUtil.parseObj(response);

            // 验证 audience
            String aud = json.getStr("aud");
            if (StrUtil.isNotBlank(googleClientId) && !googleClientId.equals(aud)) {
                throw new BizException(ErrorCode.CLIENT_ERROR, "Google 认证失败，请重试");
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
            throw new BizException(ErrorCode.THIRD_PARTY_ERROR, "Google 认证失败，请重试");
        }
    }

    private record GoogleUserInfo(String googleId, String email, String name) {}
}
