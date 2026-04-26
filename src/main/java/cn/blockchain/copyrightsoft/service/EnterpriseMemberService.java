package cn.blockchain.copyrightsoft.service;

import cn.blockchain.copyrightsoft.dto.EnterpriseMemberUpsertRequest;
import cn.blockchain.copyrightsoft.entity.User;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public interface EnterpriseMemberService {
    Page<User> getMembers(Integer page, Integer size, String keyword);

    User createMember(EnterpriseMemberUpsertRequest request);

    User updateMember(Long memberId, EnterpriseMemberUpsertRequest request);

    void updateMemberStatus(Long memberId, Integer status);

    void updateLegalScope(Long memberId, String legalScope);
}
