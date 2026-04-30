package cn.blockchain.copyrightsoft;

import cn.blockchain.copyrightsoft.dto.QueryResult;
import cn.blockchain.copyrightsoft.dto.AdminUserUpsertRequest;
import cn.blockchain.copyrightsoft.entity.Enterprise;
import cn.blockchain.copyrightsoft.entity.User;
import cn.blockchain.copyrightsoft.mapper.EnterpriseMapper;
import cn.blockchain.copyrightsoft.service.AuthService;
import cn.blockchain.copyrightsoft.service.CopyrightService;
import cn.blockchain.copyrightsoft.utils.Result;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Slf4j
@RestController
@RequestMapping("/api/admin")
/**
 * 管理后台接口。
 * <p>
 * 聚合账号管理、版权记录查询、统计信息与企业检索能力。
 * 该控制器仅做请求编排与异常映射，核心业务规则下沉到 service 层。
 */
public class AdminController {

    @Autowired
    private AuthService authService;

    @Autowired
    private CopyrightService copyrightService;

    @Autowired
    private EnterpriseMapper enterpriseMapper;

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

    @PutMapping("/users/{id}/role")
    public Result<String> updateUserRole(@PathVariable Long id,
                                         @RequestParam String role,
                                         @RequestParam(required = false) Long enterpriseId) {
        try {
            authService.updateUserRole(id, role, enterpriseId);
            return Result.success("用户角色更新成功");
        } catch (IllegalArgumentException e) {
            log.warn("更新用户角色参数错误: {}", e.getMessage());
            return Result.error(400, e.getMessage());
        } catch (NoSuchElementException e) {
            log.warn("更新用户角色资源不存在: {}", e.getMessage());
            return Result.error(404, e.getMessage());
        } catch (IllegalStateException e) {
            log.warn("更新用户角色状态冲突: {}", e.getMessage());
            return Result.error(409, e.getMessage());
        } catch (Exception e) {
            log.error("更新用户角色失败", e);
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
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String bizStatus) {
        try {
            Page<QueryResult> resultPage = copyrightService.getAllCopyrights(page, size, keyword, bizStatus);
            return Result.success(resultPage);
        } catch (Exception e) {
            log.error("获取版权列表失败", e);
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/users")
    public Result<User> createUser(@RequestBody AdminUserUpsertRequest request) {
        try {
            User user = authService.createUserByAdmin(request);
            return Result.success("账号创建成功", user);
        } catch (Exception e) {
            log.error("创建账号失败", e);
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/users/{id}")
    public Result<User> updateUser(@PathVariable Long id, @RequestBody AdminUserUpsertRequest request) {
        try {
            User user = authService.updateUserByAdmin(id, request);
            return Result.success("账号更新成功", user);
        } catch (Exception e) {
            log.error("更新账号失败", e);
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/users/{id}")
    public Result<String> deleteUser(@PathVariable Long id) {
        try {
            authService.deleteUserByAdmin(id);
            return Result.success("账号删除成功");
        } catch (Exception e) {
            log.error("删除账号失败", e);
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

    @GetMapping("/enterprises")
    public Result<List<Enterprise>> getEnterprises(@RequestParam(required = false) String keyword) {
        try {
            LambdaQueryWrapper<Enterprise> wrapper = new LambdaQueryWrapper<>();
            if (StringUtils.hasText(keyword)) {
                wrapper.like(Enterprise::getName, keyword)
                        .or()
                        .like(Enterprise::getLicenseNo, keyword);
            }
            wrapper.eq(Enterprise::getStatus, 1)
                    .orderByDesc(Enterprise::getCreatedAt)
                    .last("limit 20");
            return Result.success(enterpriseMapper.selectList(wrapper));
        } catch (Exception e) {
            log.error("获取企业列表失败", e);
            return Result.error(e.getMessage());
        }
    }
}
