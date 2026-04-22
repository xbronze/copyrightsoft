package cn.blockchain.copyrightsoft;

import cn.blockchain.copyrightsoft.dto.ApplyCopyrightRequest;
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

    @GetMapping("/query/id/{id}")
    public Result<QueryResult> queryById(@PathVariable Long id) {
        try {
            QueryResult result = copyrightService.queryById(id);
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
            @RequestParam(required = false) String keyword) {
        try {
            Page<QueryResult> result = copyrightService.getMyRecords(page, size, keyword);
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取版权记录列表失败", e);
            return Result.error(e.getMessage());
        }
    }
}
