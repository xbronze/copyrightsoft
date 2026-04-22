package cn.blockchain.copyrightsoft.service;

import cn.blockchain.copyrightsoft.dto.*;
import cn.blockchain.copyrightsoft.entity.User;

public interface AuthService {
    LoginResponse login(LoginRequest request);
    
    void register(RegisterRequest request);
    
    String getCurrentUsername();
    
    Long getCurrentUserId();

    User getCurrentUser();

    void updateProfile(UpdateProfileRequest request);

    void changePassword(ChangePasswordRequest request);
}
