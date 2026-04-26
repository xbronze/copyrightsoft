package cn.blockchain.copyrightsoft.dto;

import lombok.Data;

@Data
public class EnterpriseMemberUpsertRequest {
    private String username;
    private String password;
    private String nickname;
    private String email;
    private String phone;
    private String enterpriseRole;
    private String enterpriseLegalScope;
    private Integer status;
}
