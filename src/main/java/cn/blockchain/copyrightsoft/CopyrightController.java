package cn.blockchain.copyrightsoft;

import cn.blockchain.copyrightsoft.dto.ApplyCopyrightRequest;
import cn.blockchain.copyrightsoft.dto.ApplicationStatusResponse;
import cn.blockchain.copyrightsoft.dto.ApplicationSubmitResponse;
import cn.blockchain.copyrightsoft.dto.QueryResult;
import cn.blockchain.copyrightsoft.service.CopyrightService;
import cn.blockchain.copyrightsoft.utils.Result;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/copyright")
/**
 * 版权申请与查询接口。
 * <p>
 * 包含申请提交流程、申请状态查询、哈希查询、申请号查询及个人/企业记录分页查询。
 */
public class CopyrightController {

    private final CopyrightService copyrightService;

    public CopyrightController(CopyrightService copyrightService) {
        this.copyrightService = copyrightService;
    }

    @PostMapping("/apply")
    public Result<String> applyCopyright(@RequestParam("file") MultipartFile file,
                                         @RequestParam("softwareName") String softwareName,
                                         @RequestParam(value = "description", required = false) String description) {
        try {
            ApplyCopyrightRequest request = new ApplyCopyrightRequest();
            request.setSoftwareName(softwareName);
            request.setDescription(description);

            String txHash = copyrightService.applyCopyright(file, request);
            return Result.success("版权存证成功", txHash);
        } catch (Exception e) {
            log.error("版权存证失败", e);
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/applications")
    public Result<ApplicationSubmitResponse> submitApplication(@RequestParam("file") MultipartFile file,
                                                               @RequestParam("softwareName") String softwareName,
                                                               @RequestParam(value = "description", required = false) String description) {
        try {
            ApplyCopyrightRequest request = new ApplyCopyrightRequest();
            request.setSoftwareName(softwareName);
            request.setDescription(description);
            ApplicationSubmitResponse response = copyrightService.submitApplication(file, request);
            return Result.success("申请已提交", response);
        } catch (Exception e) {
            log.error("提交申请失败", e);
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/applications/{applicationNo}")
    public Result<ApplicationStatusResponse> getApplicationStatus(@PathVariable String applicationNo) {
        try {
            ApplicationStatusResponse response = copyrightService.getApplicationStatus(applicationNo);
            if (response == null) {
                return Result.error("申请不存在");
            }
            return Result.success(response);
        } catch (Exception e) {
            log.error("查询申请状态失败", e);
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/query/hash/{fileHash}")
    public Result<QueryResult> queryByHash(@PathVariable String fileHash) {
        try {
            QueryResult result = copyrightService.queryByHash(fileHash);
            if (result == null) {
                return Result.error("未找到版权记录");
            }
            return Result.success(result);
        } catch (Exception e) {
            log.error("查询失败", e);
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/query/application/{applicationNo}")
    public Result<QueryResult> queryByApplicationNo(@PathVariable String applicationNo) {
        try {
            QueryResult result = copyrightService.queryByApplicationNo(applicationNo);
            if (result == null) {
                return Result.error("未找到版权记录");
            }
            return Result.success(result);
        } catch (Exception e) {
            log.error("查询失败", e);
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/my-records")
    public Result<Page<QueryResult>> getMyRecords(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String bizStatus) {
        try {
            Page<QueryResult> result = copyrightService.getMyRecords(page, size, keyword, bizStatus);
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取版权记录列表失败", e);
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/enterprise-records")
    public Result<Page<QueryResult>> getEnterpriseRecords(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String bizStatus) {
        try {
            Page<QueryResult> result = copyrightService.getEnterpriseRecords(page, size, keyword, bizStatus);
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取企业版权记录列表失败", e);
            return Result.error(e.getMessage());
        }
    }
}
