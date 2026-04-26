package cn.blockchain.copyrightsoft.dto;

import lombok.Data;

@Data
public class ApplicationSubmitResponse {
    private String applicationNo;
    private String status;
    private String txHash;
}
