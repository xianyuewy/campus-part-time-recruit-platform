package org.example.col_stu_ptj_sys.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.example.col_stu_ptj_sys.dto.PageResponse;
import org.example.col_stu_ptj_sys.dto.notification.NotificationVO;
import org.example.col_stu_ptj_sys.entity.SystemNotification;
import org.example.col_stu_ptj_sys.entity.User;
import org.example.col_stu_ptj_sys.exception.BusinessException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class NotificationCenterService {

    private final SystemNotificationService systemNotificationService;
    private final UserService userService;

    public PageResponse<NotificationVO> myNotifications(long current, long size) {
        Long uid = currentUserId();
        var page = systemNotificationService.lambdaQuery()
                .eq(SystemNotification::getUserId, uid)
                .orderByDesc(SystemNotification::getCreateTime)
                .page(new Page<>(current, size));
        Page<NotificationVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(page.getRecords().stream().map(this::toVo).toList());
        return PageResponse.of(voPage);
    }

    public long unreadCount() {
        return systemNotificationService.lambdaQuery()
                .eq(SystemNotification::getUserId, currentUserId())
                .eq(SystemNotification::getReadFlag, 0)
                .count();
    }

    @Transactional
    public void markRead(Long id) {
        SystemNotification n = systemNotificationService.getById(id);
        if (n == null || !currentUserId().equals(n.getUserId())) {
            throw new BusinessException(404, "通知不存在");
        }
        if (n.getReadFlag() != null && n.getReadFlag() == 1) {
            return;
        }
        n.setReadFlag(1);
        systemNotificationService.updateById(n);
    }

    @Transactional
    public void markAllRead() {
        systemNotificationService.lambdaUpdate()
                .eq(SystemNotification::getUserId, currentUserId())
                .set(SystemNotification::getReadFlag, 1)
                .update();
    }

    @Transactional
    public void notifyUser(Long userId, String title, String content, String bizType, String bizId) {
        if (userId == null) {
            return;
        }
        SystemNotification n = new SystemNotification();
        n.setUserId(userId);
        n.setTitle(StringUtils.hasText(title) ? title.trim() : "系统通知");
        n.setContent(StringUtils.hasText(content) ? content.trim() : "您有一条新通知");
        n.setBizType(StringUtils.hasText(bizType) ? bizType.trim() : null);
        n.setBizId(StringUtils.hasText(bizId) ? bizId.trim() : null);
        n.setReadFlag(0);
        systemNotificationService.save(n);
    }

    private NotificationVO toVo(SystemNotification n) {
        return NotificationVO.builder()
                .id(n.getId())
                .title(n.getTitle())
                .content(n.getContent())
                .bizType(n.getBizType())
                .bizId(n.getBizId())
                .read(n.getReadFlag() != null && n.getReadFlag() == 1)
                .createTime(n.getCreateTime())
                .build();
    }

    private Long currentUserId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User u = userService.findByUsername(username).orElseThrow(() -> new BusinessException("用户不存在"));
        return u.getId();
    }
}
