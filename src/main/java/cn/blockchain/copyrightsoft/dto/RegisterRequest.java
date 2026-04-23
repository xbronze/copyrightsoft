package cn.blockchain.copyrightsoft.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String password;
    private String email;
    private String phone;
    private String nickname;
    private String accountType;
    private String enterpriseName;
    private String enterpriseLicenseNo;
}
