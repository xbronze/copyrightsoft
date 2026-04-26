package cn.blockchain.copyrightsoft.dto;

import lombok.Data;

@Data
public class ApplicationStatusResponse {
    private String applicationNo;
    private String softwareName;
    private String status;
    private Integer similarityScore;
    private String riskLevel;
    private String riskReason;
    private String txHash;
    private Long blockNumber;
    private String errorMessage;
}
