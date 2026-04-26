package cn.blockchain.copyrightsoft.dto;

import lombok.Data;

@Data
public class AdminUserUpsertRequest {
    private String username;
    private String password;
    private String nickname;
    private String email;
    private String phone;
    private String role;
    private String accountType;
    private Long enterpriseId;
    private Integer status;
}
