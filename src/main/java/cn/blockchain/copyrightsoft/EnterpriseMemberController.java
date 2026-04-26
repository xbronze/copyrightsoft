package cn.blockchain.copyrightsoft;

import cn.blockchain.copyrightsoft.dto.EnterpriseMemberUpsertRequest;
import cn.blockchain.copyrightsoft.entity.User;
import cn.blockchain.copyrightsoft.service.EnterpriseMemberService;
import cn.blockchain.copyrightsoft.utils.Result;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/enterprise")
public class EnterpriseMemberController {
    private final EnterpriseMemberService enterpriseMemberService;

    public EnterpriseMemberController(EnterpriseMemberService enterpriseMemberService) {
        this.enterpriseMemberService = enterpriseMemberService;
    }

    @GetMapping("/members")
    public Result<Page<User>> getMembers(@RequestParam(defaultValue = "1") Integer page,
                                         @RequestParam(defaultValue = "10") Integer size,
                                         @RequestParam(required = false) String keyword) {
        try {
            return Result.success(enterpriseMemberService.getMembers(page, size, keyword));
        } catch (Exception e) {
            log.error("获取企业成员失败", e);
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/members")
    public Result<User> createMember(@RequestBody EnterpriseMemberUpsertRequest request) {
        try {
            return Result.success("成员创建成功", enterpriseMemberService.createMember(request));
        } catch (Exception e) {
            log.error("创建企业成员失败", e);
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/members/{id}")
    public Result<User> updateMember(@PathVariable Long id, @RequestBody EnterpriseMemberUpsertRequest request) {
        try {
            return Result.success("成员更新成功", enterpriseMemberService.updateMember(id, request));
        } catch (Exception e) {
            log.error("更新企业成员失败", e);
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/members/{id}/status")
    public Result<String> updateMemberStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        try {
            enterpriseMemberService.updateMemberStatus(id, body.get("status"));
            return Result.success("成员状态更新成功");
        } catch (Exception e) {
            log.error("更新企业成员状态失败", e);
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/members/{id}/legal-scope")
    public Result<String> updateLegalScope(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            enterpriseMemberService.updateLegalScope(id, body.get("enterpriseLegalScope"));
            return Result.success("法务可见范围更新成功");
        } catch (Exception e) {
            log.error("更新法务可见范围失败", e);
            return Result.error(e.getMessage());
        }
    }
}
