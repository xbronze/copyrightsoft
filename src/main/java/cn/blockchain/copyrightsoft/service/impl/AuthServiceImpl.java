package cn.blockchain.copyrightsoft.service.impl;

import cn.blockchain.copyrightsoft.auth.AuthDomainRules;
import cn.blockchain.copyrightsoft.dto.*;
import cn.blockchain.copyrightsoft.entity.Enterprise;
import cn.blockchain.copyrightsoft.entity.User;
import cn.blockchain.copyrightsoft.mapper.EnterpriseMapper;
import cn.blockchain.copyrightsoft.mapper.UserMapper;
import cn.blockchain.copyrightsoft.service.AuthService;
import cn.blockchain.copyrightsoft.utils.JwtUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EnterpriseMapper enterpriseMapper;

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userMapper.selectOne(
            new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername())
        );

        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }

        if (user.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        String normalizedRole = AuthDomainRules.normalizeRole(user.getRole());
        String accountType = user.getAccountType();
        if (accountType == null && AuthDomainRules.ROLE_USER_LEGACY.equals(user.getRole())) {
            accountType = AuthDomainRules.ACCOUNT_TYPE_INDIVIDUAL;
        }
        if (accountType != null && !AuthDomainRules.isRoleCompatibleWithAccountType(accountType, user.getRole())
                && !AuthDomainRules.isPlatformRole(user.getRole())) {
            throw new RuntimeException("账号主体与角色不匹配，请联系管理员");
        }

        String token = jwtUtils.generateToken(
                user.getUsername(),
                user.getId(),
                normalizedRole,
                accountType,
                user.getEnterpriseId()
        );

        return new LoginResponse(
                token,
                user.getUsername(),
                normalizedRole,
                user.getId(),
                accountType,
                user.getEnterpriseId(),
                user.getEnterpriseRole(),
                user.getEnterpriseLegalScope()
        );
    }

    @Override
    public void register(RegisterRequest request) {
        registerIndividualUser(request);
    }

    @Override
    public void registerEnterprise(RegisterRequest request) {
        validateUsernameNotExists(request.getUsername());
        if (!StringUtils.hasText(request.getEnterpriseName())) {
            throw new RuntimeException("企业名称不能为空");
        }

        Enterprise enterprise = new Enterprise();
        enterprise.setName(request.getEnterpriseName());
        enterprise.setLicenseNo(request.getEnterpriseLicenseNo());
        enterprise.setStatus(1);
        enterpriseMapper.insert(enterprise);

        User user = createBaseUser(request);
        user.setAccountType(AuthDomainRules.ACCOUNT_TYPE_ENTERPRISE);
        user.setRole(AuthDomainRules.ROLE_ENTERPRISE_DEVELOPER);
        user.setEnterpriseId(enterprise.getId());
        user.setEnterpriseRole(AuthDomainRules.ENTERPRISE_ROLE_OWNER);
        user.setEnterpriseLegalScope(AuthDomainRules.ENTERPRISE_LEGAL_SCOPE_ALL);
        user.setDisplaySubjectName(enterprise.getName());
        userMapper.insert(user);
    }

    private void registerIndividualUser(RegisterRequest request) {
        String requestedAccountType = request.getAccountType();
        if (StringUtils.hasText(requestedAccountType)
                && !AuthDomainRules.ACCOUNT_TYPE_INDIVIDUAL.equals(requestedAccountType)) {
            throw new RuntimeException("个人注册仅支持 INDIVIDUAL 主体");
        }
        validateUsernameNotExists(request.getUsername());
        User user = createBaseUser(request);
        user.setRole(AuthDomainRules.ROLE_INDIVIDUAL_DEVELOPER);
        user.setAccountType(AuthDomainRules.ACCOUNT_TYPE_INDIVIDUAL);
        user.setEnterpriseRole(null);
        user.setEnterpriseLegalScope(AuthDomainRules.ENTERPRISE_LEGAL_SCOPE_SELF);
        user.setDisplaySubjectName(user.getNickname());
        userMapper.insert(user);
    }

    private void validateUsernameNotExists(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        
        if (userMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("用户名已存在");
        }
    }

    private User createBaseUser(RegisterRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());
        user.setStatus(1);
        return user;
    }

    @Override
    public String getCurrentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    @Override
    public Long getCurrentUserId() {
        String username = getCurrentUsername();
        User user = userMapper.selectOne(
            new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)
        );
        return user != null ? user.getId() : null;
    }

    @Override
    public User getCurrentUser() {
        String username = getCurrentUsername();
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, username)
        );
        return user;
    }

    @Override
    public void updateProfile(UpdateProfileRequest request) {
        String username = getCurrentUsername();
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, username)
        );

        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 更新字段
        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }

        userMapper.updateById(user);
    }

    @Override
    public void changePassword(ChangePasswordRequest request) {
        String username = getCurrentUsername();
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, username)
        );

        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 验证旧密码
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("原密码错误");
        }

        // 验证新密码一致性
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("两次输入的新密码不一致");
        }

        // 验证新密码长度
        if (request.getNewPassword().length() < 6) {
            throw new RuntimeException("新密码长度不能少于6位");
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userMapper.updateById(user);
    }

    @Override
    public Page<User> getAllUsers(Integer page, Integer size, String keyword) {
        Page<User> userPage = new Page<>(page, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            wrapper.like(User::getUsername, keyword)
                    .or()
                    .like(User::getNickname, keyword)
                    .or()
                    .like(User::getEmail, keyword);
        }

        wrapper.ne(User::getRole, AuthDomainRules.ROLE_ADMIN)
                .orderByDesc(User::getCreatedAt);

        return userMapper.selectPage(userPage, wrapper);
    }

    @Override
    public void updateUserStatus(Long userId, Integer status) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (AuthDomainRules.ROLE_ADMIN.equals(user.getRole())) {
            throw new RuntimeException("不能修改管理员状态");
        }
        user.setStatus(status);
        userMapper.updateById(user);
    }

    @Override
    public void updateUserRole(Long userId, String role, Long enterpriseId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new NoSuchElementException("用户不存在");
        }
        if (!StringUtils.hasText(role)) {
            throw new IllegalArgumentException("角色不能为空");
        }
        if (role.equals(user.getRole()) &&
                (!AuthDomainRules.ROLE_ENTERPRISE_DEVELOPER.equals(role) || enterpriseId != null && enterpriseId.equals(user.getEnterpriseId()))) {
            throw new IllegalStateException("角色未发生变化");
        }
        if (AuthDomainRules.ROLE_USER_LEGACY.equals(role)) {
            throw new IllegalArgumentException("不支持分配旧角色 USER");
        }

        if (AuthDomainRules.ROLE_INDIVIDUAL_DEVELOPER.equals(role)) {
            user.setRole(role);
            user.setAccountType(AuthDomainRules.ACCOUNT_TYPE_INDIVIDUAL);
            user.setEnterpriseId(null);
            user.setEnterpriseRole(null);
            user.setEnterpriseLegalScope(AuthDomainRules.ENTERPRISE_LEGAL_SCOPE_SELF);
            if (!StringUtils.hasText(user.getDisplaySubjectName())) {
                user.setDisplaySubjectName(user.getNickname());
            }
        } else if (AuthDomainRules.ROLE_ENTERPRISE_DEVELOPER.equals(role) || AuthDomainRules.ROLE_ENTERPRISE_LEGAL.equals(role)) {
            if (enterpriseId == null) {
                throw new IllegalArgumentException("企业开发者必须提供 enterpriseId");
            }
            Enterprise enterprise = enterpriseMapper.selectById(enterpriseId);
            if (enterprise == null) {
                throw new NoSuchElementException("企业不存在");
            }
            if (enterprise.getStatus() == null || enterprise.getStatus() != 1) {
                throw new IllegalStateException("企业已禁用，不能分配企业开发者");
            }
            user.setRole(role);
            user.setAccountType(AuthDomainRules.ACCOUNT_TYPE_ENTERPRISE);
            user.setEnterpriseId(enterpriseId);
            if (AuthDomainRules.ROLE_ENTERPRISE_LEGAL.equals(role)) {
                user.setEnterpriseRole(AuthDomainRules.ENTERPRISE_ROLE_LEGAL);
                user.setEnterpriseLegalScope(AuthDomainRules.ENTERPRISE_LEGAL_SCOPE_SELF);
            } else {
                user.setEnterpriseRole(AuthDomainRules.ENTERPRISE_ROLE_DEVELOPER);
                user.setEnterpriseLegalScope(AuthDomainRules.ENTERPRISE_LEGAL_SCOPE_SELF);
            }
            user.setDisplaySubjectName(enterprise.getName());
        } else if (AuthDomainRules.ROLE_AUDITOR.equals(role) || AuthDomainRules.ROLE_ADMIN.equals(role)) {
            user.setRole(role);
            if (!StringUtils.hasText(user.getAccountType())) {
                user.setAccountType(AuthDomainRules.ACCOUNT_TYPE_INDIVIDUAL);
            }
            user.setEnterpriseId(null);
            user.setEnterpriseRole(null);
            user.setEnterpriseLegalScope(AuthDomainRules.ENTERPRISE_LEGAL_SCOPE_SELF);
            if (!StringUtils.hasText(user.getDisplaySubjectName())) {
                user.setDisplaySubjectName(user.getNickname());
            }
        } else {
            throw new IllegalArgumentException("不支持的角色类型");
        }

        userMapper.updateById(user);
    }

    @Override
    public String resetUserPassword(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (AuthDomainRules.ROLE_ADMIN.equals(user.getRole())) {
            throw new RuntimeException("不能重置管理员密码");
        }

        String newPassword = generateRandomPassword(8);
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);

        return newPassword;
    }

    @Override
    public Map<String, Object> getStatistics() {
        Map<String, Object> statistics = new HashMap<>();

        long totalUsers = userMapper.selectCount(
                new LambdaQueryWrapper<User>().ne(User::getRole, AuthDomainRules.ROLE_ADMIN)
        );

        statistics.put("totalUsers", totalUsers);
        statistics.put("activeUsers", userMapper.selectCount(
                new LambdaQueryWrapper<User>()
                        .ne(User::getRole, AuthDomainRules.ROLE_ADMIN)
                        .eq(User::getStatus, 1)
        ));

        return statistics;
    }

    @Override
    public User createUserByAdmin(AdminUserUpsertRequest request) {
        if (!StringUtils.hasText(request.getUsername())) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (!StringUtils.hasText(request.getPassword()) || request.getPassword().length() < 6) {
            throw new IllegalArgumentException("密码不能为空且长度至少6位");
        }
        validateUsernameNotExists(request.getUsername());

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(StringUtils.hasText(request.getNickname()) ? request.getNickname() : request.getUsername());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setStatus(request.getStatus() == null ? 1 : request.getStatus());

        applyRoleAndSubject(user, request.getRole(), request.getEnterpriseId());
        userMapper.insert(user);
        user.setPassword(null);
        return user;
    }

    @Override
    public User updateUserByAdmin(Long userId, AdminUserUpsertRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new NoSuchElementException("用户不存在");
        }
        if (AuthDomainRules.ROLE_ADMIN.equals(user.getRole())) {
            throw new IllegalArgumentException("不能修改管理员账号");
        }

        if (StringUtils.hasText(request.getNickname())) {
            user.setNickname(request.getNickname());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }
        if (StringUtils.hasText(request.getPassword())) {
            if (request.getPassword().length() < 6) {
                throw new IllegalArgumentException("密码长度不能少于6位");
            }
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (StringUtils.hasText(request.getRole())) {
            applyRoleAndSubject(user, request.getRole(), request.getEnterpriseId());
        }

        userMapper.updateById(user);
        user.setPassword(null);
        return user;
    }

    @Override
    public void deleteUserByAdmin(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new NoSuchElementException("用户不存在");
        }
        if (AuthDomainRules.ROLE_ADMIN.equals(user.getRole())) {
            throw new IllegalArgumentException("不能删除管理员账号");
        }
        userMapper.deleteById(userId);
    }

    private void applyRoleAndSubject(User user, String role, Long enterpriseId) {
        if (!StringUtils.hasText(role)) {
            throw new IllegalArgumentException("角色不能为空");
        }
        if (AuthDomainRules.ROLE_USER_LEGACY.equals(role)) {
            throw new IllegalArgumentException("不支持分配旧角色 USER");
        }
        if (AuthDomainRules.ROLE_INDIVIDUAL_DEVELOPER.equals(role)) {
            user.setRole(role);
            user.setAccountType(AuthDomainRules.ACCOUNT_TYPE_INDIVIDUAL);
            user.setEnterpriseId(null);
            user.setEnterpriseRole(null);
            user.setEnterpriseLegalScope(AuthDomainRules.ENTERPRISE_LEGAL_SCOPE_SELF);
            user.setDisplaySubjectName(StringUtils.hasText(user.getNickname()) ? user.getNickname() : user.getUsername());
            return;
        }
        if (AuthDomainRules.ROLE_ENTERPRISE_DEVELOPER.equals(role) || AuthDomainRules.ROLE_ENTERPRISE_LEGAL.equals(role)) {
            if (enterpriseId == null) {
                throw new IllegalArgumentException("企业开发者必须提供 enterpriseId");
            }
            Enterprise enterprise = enterpriseMapper.selectById(enterpriseId);
            if (enterprise == null) {
                throw new NoSuchElementException("企业不存在");
            }
            if (enterprise.getStatus() == null || enterprise.getStatus() != 1) {
                throw new IllegalStateException("企业已禁用，不能分配企业开发者");
            }
            user.setRole(role);
            user.setAccountType(AuthDomainRules.ACCOUNT_TYPE_ENTERPRISE);
            user.setEnterpriseId(enterpriseId);
            if (AuthDomainRules.ROLE_ENTERPRISE_LEGAL.equals(role)) {
                user.setEnterpriseRole(AuthDomainRules.ENTERPRISE_ROLE_LEGAL);
                user.setEnterpriseLegalScope(AuthDomainRules.ENTERPRISE_LEGAL_SCOPE_SELF);
            } else {
                user.setEnterpriseRole(AuthDomainRules.ENTERPRISE_ROLE_DEVELOPER);
                user.setEnterpriseLegalScope(AuthDomainRules.ENTERPRISE_LEGAL_SCOPE_SELF);
            }
            user.setDisplaySubjectName(enterprise.getName());
            return;
        }
        if (AuthDomainRules.ROLE_AUDITOR.equals(role)) {
            user.setRole(role);
            user.setAccountType(AuthDomainRules.ACCOUNT_TYPE_INDIVIDUAL);
            user.setEnterpriseId(null);
            user.setEnterpriseRole(null);
            user.setEnterpriseLegalScope(AuthDomainRules.ENTERPRISE_LEGAL_SCOPE_SELF);
            user.setDisplaySubjectName(StringUtils.hasText(user.getNickname()) ? user.getNickname() : user.getUsername());
            return;
        }
        throw new IllegalArgumentException("不支持的角色类型");
    }

    private String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < length; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        return password.toString();
    }
}
