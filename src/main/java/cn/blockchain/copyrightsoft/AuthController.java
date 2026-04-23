package cn.blockchain.copyrightsoft;

import cn.blockchain.copyrightsoft.dto.*;
import cn.blockchain.copyrightsoft.entity.User;
import cn.blockchain.copyrightsoft.service.AuthService;
import cn.blockchain.copyrightsoft.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.login(request);
            return Result.success("登录成功", response);
        } catch (Exception e) {
            log.error("登录失败", e);
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/register")
    public Result<String> register(@RequestBody RegisterRequest request) {
        try {
            authService.register(request);
            return Result.success("注册成功");
        } catch (Exception e) {
            log.error("注册失败", e);
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/register/enterprise")
    public Result<String> registerEnterprise(@RequestBody RegisterRequest request) {
        try {
            authService.registerEnterprise(request);
            return Result.success("企业账号注册成功");
        } catch (Exception e) {
            log.error("企业注册失败", e);
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/info")
    public Result<User> getUserInfo() {
        try {
            User user = authService.getCurrentUser();
            if (user == null) {
                return Result.error("用户不存在");
            }
            // 不返回密码
            user.setPassword(null);
            return Result.success(user);
        } catch (Exception e) {
            log.error("获取用户信息失败", e);
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/profile")
    public Result<String> updateProfile(@RequestBody UpdateProfileRequest request) {
        try {
            authService.updateProfile(request);
            return Result.success("个人信息更新成功");
        } catch (Exception e) {
            log.error("更新个人信息失败", e);
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/password")
    public Result<String> changePassword(@RequestBody ChangePasswordRequest request) {
        try {
            authService.changePassword(request);
            return Result.success("密码修改成功");
        } catch (Exception e) {
            log.error("修改密码失败", e);
            return Result.error(e.getMessage());
        }
    }
}
