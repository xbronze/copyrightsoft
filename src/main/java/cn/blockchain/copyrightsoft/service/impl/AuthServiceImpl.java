package cn.blockchain.copyrightsoft.service.impl;

import cn.blockchain.copyrightsoft.dto.*;
import cn.blockchain.copyrightsoft.entity.User;
import cn.blockchain.copyrightsoft.mapper.UserMapper;
import cn.blockchain.copyrightsoft.service.AuthService;
import cn.blockchain.copyrightsoft.utils.JwtUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

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

        String token = jwtUtils.generateToken(user.getUsername(), user.getId(), user.getRole());

        return new LoginResponse(token, user.getUsername(), user.getRole(), user.getId());
    }

    @Override
    public void register(RegisterRequest request) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, request.getUsername());
        
        if (userMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("用户名已存在");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());
        user.setRole("USER");
        user.setStatus(1);

        userMapper.insert(user);
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
}
