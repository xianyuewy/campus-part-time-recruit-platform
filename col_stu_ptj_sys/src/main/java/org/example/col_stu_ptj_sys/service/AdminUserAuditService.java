package org.example.col_stu_ptj_sys.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.col_stu_ptj_sys.dto.PageResponse;
import org.example.col_stu_ptj_sys.dto.admin.UserAuditVO;
import org.example.col_stu_ptj_sys.entity.User;
import org.example.col_stu_ptj_sys.enums.AuthStatus;
import org.example.col_stu_ptj_sys.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminUserAuditService {

    private final UserService userService;

    public PageResponse<UserAuditVO> pagePendingUsers(long current, long size) {
        Page<User> page = new Page<>(current, size);
        Page<User> result = userService.lambdaQuery()
                .eq(User::getAuthStatus, AuthStatus.PENDING)
                .orderByDesc(User::getCreateTime)
                .page(page);
        Page<UserAuditVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(this::toVo).toList());
        return PageResponse.of(voPage);
    }

    @Transactional
    public void approve(Long userId) {
        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        if (user.getAuthStatus() != AuthStatus.PENDING) {
            throw new BusinessException("当前状态不可审核通过");
        }
        user.setAuthStatus(AuthStatus.APPROVED);
        user.setAuthTime(LocalDateTime.now());
        if (!userService.updateById(user)) {
            throw new BusinessException("更新失败");
        }
    }

    @Transactional
    public void reject(Long userId, String remark) {
        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        if (user.getAuthStatus() != AuthStatus.PENDING) {
            throw new BusinessException("当前状态不可驳回");
        }
        user.setAuthStatus(AuthStatus.REJECTED);
        user.setAuthRemark(remark);
        user.setAuthTime(LocalDateTime.now());
        if (!userService.updateById(user)) {
            throw new BusinessException("更新失败");
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
