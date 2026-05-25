package org.example.col_stu_ptj_sys.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.col_stu_ptj_sys.dto.PageResponse;
import org.example.col_stu_ptj_sys.dto.admin.UserAuditVO;
import org.example.col_stu_ptj_sys.entity.User;
import org.example.col_stu_ptj_sys.enums.UserRole;
import org.example.col_stu_ptj_sys.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AdminUserManagementService {

    private final UserService userService;

    public PageResponse<UserAuditVO> pageUsers(
            long current,
            long size,
            String role,
            String keyword,
            Boolean accountEnabled) {
        Page<User> page = new Page<>(current, size);
        var q = userService.lambdaQuery();
        if (StringUtils.hasText(role)) {
            q.eq(User::getRole, UserRole.valueOf(role.toUpperCase()));
        }
        if (StringUtils.hasText(keyword)) {
            q.and(w -> w.like(User::getUsername, keyword).or().like(User::getEmail, keyword));
        }
        if (accountEnabled != null) {
            q.eq(User::getAccountEnabled, accountEnabled);
        }
        q.orderByDesc(User::getCreateTime);
        Page<User> result = q.page(page);
        Page<UserAuditVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(this::toVo).toList());
        return PageResponse.of(voPage);
    }

    @Transactional
    public void disable(Long userId) {
        User user = requireUser(userId);
        assertCanManage(user);
        if (user.getRole() == UserRole.ADMIN) {
            throw new BusinessException("不能禁用管理员账号");
        }
        user.setAccountEnabled(false);
        if (!userService.updateById(user)) {
            throw new BusinessException("更新失败");
        }
    }

    @Transactional
    public void enable(Long userId) {
        User user = requireUser(userId);
        assertCanManage(user);
        user.setAccountEnabled(true);
        if (!userService.updateById(user)) {
            throw new BusinessException("更新失败");
        }
    }

    private User requireUser(Long userId) {
        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        return user;
    }

    private void assertCanManage(User target) {
        String me = SecurityContextHolder.getContext().getAuthentication().getName();
        if (me != null && me.equals(target.getUsername())) {
            throw new BusinessException("不能操作当前登录账号");
        }
    }

    private UserAuditVO toVo(User u) {
        return UserAuditVO.builder()
                .id(u.getId())
                .username(u.getUsername())
                .email(u.getEmail())
                .phone(u.getPhone())
                .role(u.getRole())
                .authStatus(u.getAuthStatus())
                .idCard(u.getIdCard())
                .authRemark(u.getAuthRemark())
                .createTime(u.getCreateTime())
                .accountEnabled(u.getAccountEnabled() == null || u.getAccountEnabled())
                .build();
    }
}
