package cn.blockchain.copyrightsoft;

import cn.blockchain.copyrightsoft.dto.QueryResult;
import cn.blockchain.copyrightsoft.entity.User;
import cn.blockchain.copyrightsoft.service.AuthService;
import cn.blockchain.copyrightsoft.service.CopyrightService;
import cn.blockchain.copyrightsoft.utils.Result;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AuthService authService;

    @Autowired
    private CopyrightService copyrightService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/users")
    public Result<Page<User>> getUsers(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        try {
            Page<User> userPage = authService.getAllUsers(page, size, keyword);
            userPage.getRecords().forEach(user -> user.setPassword(null));
            return Result.success(userPage);
        } catch (Exception e) {
            log.error("获取用户列表失败", e);
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/users/{id}/status")
    public Result<String> updateUserStatus(@PathVariable Long id, @RequestParam Integer status) {
        try {
            authService.updateUserStatus(id, status);
            return Result.success("用户状态更新成功");
        } catch (Exception e) {
            log.error("更新用户状态失败", e);
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/users/{id}/reset-password")
    public Result<String> resetPassword(@PathVariable Long id) {
        try {
            String newPassword = authService.resetUserPassword(id);
            return Result.success("密码重置成功，新密码为: " + newPassword);
        } catch (Exception e) {
            log.error("重置密码失败", e);
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/copyrights")
    public Result<Page<QueryResult>> getAllCopyrights(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        try {
            Page<QueryResult> resultPage = copyrightService.getAllCopyrights(page, size, keyword);
            return Result.success(resultPage);
        } catch (Exception e) {
            log.error("获取版权列表失败", e);
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics() {
        try {
            Map<String, Object> statistics = authService.getStatistics();
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("获取统计数据失败", e);
            return Result.error(e.getMessage());
        }
    }
}
