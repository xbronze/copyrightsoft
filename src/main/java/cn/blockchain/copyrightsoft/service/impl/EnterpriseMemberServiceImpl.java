package cn.blockchain.copyrightsoft.service.impl;

import cn.blockchain.copyrightsoft.auth.AuthDomainRules;
import cn.blockchain.copyrightsoft.dto.EnterpriseMemberUpsertRequest;
import cn.blockchain.copyrightsoft.entity.User;
import cn.blockchain.copyrightsoft.mapper.UserMapper;
import cn.blockchain.copyrightsoft.service.AuthService;
import cn.blockchain.copyrightsoft.service.EnterpriseMemberService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class EnterpriseMemberServiceImpl implements EnterpriseMemberService {
    private final UserMapper userMapper;
    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;

    public EnterpriseMemberServiceImpl(UserMapper userMapper, AuthService authService, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.authService = authService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Page<User> getMembers(Integer page, Integer size, String keyword) {
        User owner = requireEnterpriseOwner();
        Page<User> memberPage = new Page<>(page, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getAccountType, AuthDomainRules.ACCOUNT_TYPE_ENTERPRISE)
                .eq(User::getEnterpriseId, owner.getEnterpriseId());
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(User::getUsername, keyword)
                    .or()
                    .like(User::getNickname, keyword)
                    .or()
                    .like(User::getEmail, keyword));
        }
        wrapper.orderByDesc(User::getCreatedAt);
        Page<User> result = userMapper.selectPage(memberPage, wrapper);
        result.getRecords().forEach(member -> member.setPassword(null));
        return result;
    }

    @Override
    public User createMember(EnterpriseMemberUpsertRequest request) {
        User owner = requireEnterpriseOwner();
        if (!StringUtils.hasText(request.getUsername())) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (!StringUtils.hasText(request.getPassword()) || request.getPassword().length() < 6) {
            throw new IllegalArgumentException("密码不能为空且长度至少6位");
        }
        validateEnterpriseRole(request.getEnterpriseRole());
        LambdaQueryWrapper<User> usernameWrapper = new LambdaQueryWrapper<>();
        usernameWrapper.eq(User::getUsername, request.getUsername());
        if (userMapper.selectCount(usernameWrapper) > 0) {
            throw new IllegalArgumentException("用户名已存在");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(StringUtils.hasText(request.getNickname()) ? request.getNickname() : request.getUsername());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        user.setAccountType(AuthDomainRules.ACCOUNT_TYPE_ENTERPRISE);
        user.setEnterpriseId(owner.getEnterpriseId());
        user.setEnterpriseRole(request.getEnterpriseRole());
        user.setEnterpriseLegalScope(resolveLegalScope(request.getEnterpriseRole(), request.getEnterpriseLegalScope()));
        user.setRole(resolveGlobalRole(request.getEnterpriseRole()));
        user.setDisplaySubjectName(owner.getDisplaySubjectName());
        userMapper.insert(user);
        user.setPassword(null);
        return user;
    }

    @Override
    public User updateMember(Long memberId, EnterpriseMemberUpsertRequest request) {
        User owner = requireEnterpriseOwner();
        User member = getMemberWithinEnterprise(memberId, owner.getEnterpriseId());
        if (AuthDomainRules.ENTERPRISE_ROLE_OWNER.equals(member.getEnterpriseRole())) {
            throw new IllegalStateException("不允许编辑企业管理员账号");
        }
        if (StringUtils.hasText(request.getNickname())) {
            member.setNickname(request.getNickname());
        }
        if (request.getEmail() != null) {
            member.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            member.setPhone(request.getPhone());
        }
        if (request.getStatus() != null) {
            member.setStatus(request.getStatus());
        }
        if (StringUtils.hasText(request.getPassword())) {
            if (request.getPassword().length() < 6) {
                throw new IllegalArgumentException("密码长度不能少于6位");
            }
            member.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (StringUtils.hasText(request.getEnterpriseRole())) {
            validateEnterpriseRole(request.getEnterpriseRole());
            member.setEnterpriseRole(request.getEnterpriseRole());
            member.setRole(resolveGlobalRole(request.getEnterpriseRole()));
            member.setEnterpriseLegalScope(resolveLegalScope(request.getEnterpriseRole(), request.getEnterpriseLegalScope()));
        } else if (StringUtils.hasText(request.getEnterpriseLegalScope())
                && AuthDomainRules.ENTERPRISE_ROLE_LEGAL.equals(member.getEnterpriseRole())) {
            member.setEnterpriseLegalScope(resolveLegalScope(member.getEnterpriseRole(), request.getEnterpriseLegalScope()));
        }
        userMapper.updateById(member);
        member.setPassword(null);
        return member;
    }

    @Override
    public void updateMemberStatus(Long memberId, Integer status) {
        User owner = requireEnterpriseOwner();
        User member = getMemberWithinEnterprise(memberId, owner.getEnterpriseId());
        if (AuthDomainRules.ENTERPRISE_ROLE_OWNER.equals(member.getEnterpriseRole())) {
            throw new IllegalStateException("不允许禁用企业管理员账号");
        }
        member.setStatus(status);
        userMapper.updateById(member);
    }

    @Override
    public void updateLegalScope(Long memberId, String legalScope) {
        User owner = requireEnterpriseOwner();
        User member = getMemberWithinEnterprise(memberId, owner.getEnterpriseId());
        if (!AuthDomainRules.ENTERPRISE_ROLE_LEGAL.equals(member.getEnterpriseRole())) {
            throw new IllegalArgumentException("仅法务角色可配置可见范围");
        }
        if (!AuthDomainRules.isEnterpriseLegalScopeValid(legalScope)) {
            throw new IllegalArgumentException("法务可见范围不合法");
        }
        member.setEnterpriseLegalScope(legalScope);
        userMapper.updateById(member);
    }

    private User requireEnterpriseOwner() {
        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("当前用户未登录");
        }
        if (!AuthDomainRules.ACCOUNT_TYPE_ENTERPRISE.equals(currentUser.getAccountType())
                || !AuthDomainRules.ENTERPRISE_ROLE_OWNER.equals(currentUser.getEnterpriseRole())) {
            throw new IllegalStateException("仅企业管理员可执行此操作");
        }
        return currentUser;
    }

    private User getMemberWithinEnterprise(Long memberId, Long enterpriseId) {
        User member = userMapper.selectById(memberId);
        if (member == null) {
            throw new IllegalArgumentException("成员不存在");
        }
        if (!enterpriseId.equals(member.getEnterpriseId())) {
            throw new IllegalStateException("无权操作其他企业成员");
        }
        return member;
    }

    private void validateEnterpriseRole(String enterpriseRole) {
        if (!AuthDomainRules.isEnterpriseRoleValid(enterpriseRole)) {
            throw new IllegalArgumentException("企业角色不合法");
        }
    }

    private String resolveGlobalRole(String enterpriseRole) {
        if (AuthDomainRules.ENTERPRISE_ROLE_LEGAL.equals(enterpriseRole)) {
            return AuthDomainRules.ROLE_ENTERPRISE_LEGAL;
        }
        return AuthDomainRules.ROLE_ENTERPRISE_DEVELOPER;
    }

    private String resolveLegalScope(String enterpriseRole, String legalScope) {
        if (!AuthDomainRules.ENTERPRISE_ROLE_LEGAL.equals(enterpriseRole)) {
            return AuthDomainRules.ENTERPRISE_LEGAL_SCOPE_SELF;
        }
        if (!StringUtils.hasText(legalScope)) {
            return AuthDomainRules.ENTERPRISE_LEGAL_SCOPE_SELF;
        }
        if (!AuthDomainRules.isEnterpriseLegalScopeValid(legalScope)) {
            throw new IllegalArgumentException("法务可见范围不合法");
        }
        return legalScope;
    }
}
