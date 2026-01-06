package dev.chanler.knownote.user.api;

import dev.chanler.knownote.common.Result;
import dev.chanler.knownote.user.api.dto.req.GoogleLoginReqDTO;
import dev.chanler.knownote.user.api.dto.req.UpdatePasswordReqDTO;
import dev.chanler.knownote.user.api.dto.req.UpdateProfileReqDTO;
import dev.chanler.knownote.user.api.dto.resp.UserMeRespDTO;
import dev.chanler.knownote.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户接口
 */
@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 获取当前用户信息
     */
    @GetMapping("/me")
    public Result<UserMeRespDTO> getMe() {
        return Result.ok(userService.getMe());
    }

    /**
     * 修改个人资料
     */
    @PostMapping("/me")
    public Result<Void> updateProfile(@Valid @RequestBody UpdateProfileReqDTO req) {
        userService.updateProfile(req);
        return Result.ok();
    }

    /**
     * 修改密码
     */
    @PostMapping("/me/password")
    public Result<Void> updatePassword(@Valid @RequestBody UpdatePasswordReqDTO req) {
        userService.updatePassword(req);
        return Result.ok();
    }

    /**
     * 绑定 Google 账号
     */
    @PostMapping("/me/google")
    public Result<Void> bindGoogle(@Valid @RequestBody GoogleLoginReqDTO req) {
        userService.bindGoogle(req);
        return Result.ok();
    }
}
