package cn.blockchain.copyrightsoft.service;

import cn.blockchain.copyrightsoft.dto.ApplyCopyrightRequest;
import cn.blockchain.copyrightsoft.dto.ApplicationStatusResponse;
import cn.blockchain.copyrightsoft.dto.ApplicationSubmitResponse;
import cn.blockchain.copyrightsoft.dto.QueryResult;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.multipart.MultipartFile;

public interface CopyrightService {

    String applyCopyright(MultipartFile file, ApplyCopyrightRequest request);

    ApplicationSubmitResponse submitApplication(MultipartFile file, ApplyCopyrightRequest request);

    ApplicationStatusResponse getApplicationStatus(String applicationNo);

    QueryResult queryByHash(String fileHash);

    QueryResult queryByApplicationNo(String applicationNo);

    Page<QueryResult> getMyRecords(Integer page, Integer size, String keyword, String bizStatus);

    Page<QueryResult> getEnterpriseRecords(Integer page, Integer size, String keyword, String bizStatus);

    Page<QueryResult> getAllCopyrights(Integer page, Integer size, String keyword, String bizStatus);

    Page<QueryResult> getAuditRecords(Integer page, Integer size, String keyword, String auditStatus);

    void auditRecord(Long recordId, String auditStatus, String auditComment);

    void reviewApplication(Long applicationId, String reviewResult, String reviewNote);
}
