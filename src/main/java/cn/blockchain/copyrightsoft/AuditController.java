package cn.blockchain.copyrightsoft;

import cn.blockchain.copyrightsoft.dto.QueryResult;
import cn.blockchain.copyrightsoft.service.CopyrightService;
import cn.blockchain.copyrightsoft.utils.Result;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/audit")
public class AuditController {

    private final CopyrightService copyrightService;

    public AuditController(CopyrightService copyrightService) {
        this.copyrightService = copyrightService;
    }

    @GetMapping("/records")
    public Result<Page<QueryResult>> getAuditRecords(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String auditStatus) {
        try {
            return Result.success(copyrightService.getAuditRecords(page, size, keyword, auditStatus));
        } catch (Exception e) {
            log.error("获取审核列表失败", e);
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/records/application/{applicationNo}")
    public Result<QueryResult> getAuditDetail(@PathVariable String applicationNo) {
        try {
            QueryResult result = copyrightService.queryByApplicationNo(applicationNo);
            if (result == null) {
                return Result.error("未找到版权记录");
            }
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取审核详情失败", e);
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/records/{id}/action")
    public Result<String> auditRecord(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            copyrightService.auditRecord(id, body.get("auditStatus"), body.get("auditComment"));
            return Result.success("审核完成");
        } catch (Exception e) {
            log.error("审核失败", e);
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/applications/{id}/action")
    public Result<String> reviewApplication(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            copyrightService.reviewApplication(id, body.get("reviewResult"), body.get("reviewNote"));
            return Result.success("复核完成");
        } catch (Exception e) {
            log.error("复核失败", e);
            return Result.error(e.getMessage());
        }
    }
}
