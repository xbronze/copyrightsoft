package cn.blockchain.copyrightsoft.service;

import cn.blockchain.copyrightsoft.dto.*;
import cn.blockchain.copyrightsoft.entity.User;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.Map;

public interface AuthService {
    LoginResponse login(LoginRequest request);
    
    void register(RegisterRequest request);

    void registerEnterprise(RegisterRequest request);
    
    String getCurrentUsername();
    
    Long getCurrentUserId();

    User getCurrentUser();

    void updateProfile(UpdateProfileRequest request);

    void changePassword(ChangePasswordRequest request);

    Page<User> getAllUsers(Integer page, Integer size, String keyword);

    void updateUserStatus(Long userId, Integer status);

    void updateUserRole(Long userId, String role, Long enterpriseId);

    String resetUserPassword(Long userId);

    Map<String, Object> getStatistics();
}
