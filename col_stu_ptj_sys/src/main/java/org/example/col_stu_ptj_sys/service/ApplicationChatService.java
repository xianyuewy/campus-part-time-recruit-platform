package org.example.col_stu_ptj_sys.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.example.col_stu_ptj_sys.dto.PageResponse;
import org.example.col_stu_ptj_sys.dto.chat.ApplicationMessageVO;
import org.example.col_stu_ptj_sys.entity.ApplicationMessage;
import org.example.col_stu_ptj_sys.entity.JobApplication;
import org.example.col_stu_ptj_sys.entity.User;
import org.example.col_stu_ptj_sys.enums.ApplicationStatus;
import org.example.col_stu_ptj_sys.exception.BusinessException;
import org.example.col_stu_ptj_sys.mapper.ApplicationMessageMapper;
import org.example.col_stu_ptj_sys.websocket.ApplicationChatWebSocketHub;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ApplicationChatService {

    private static final int MAX_CONTENT = 2000;

    private final ApplicationMessageMapper applicationMessageMapper;
    private final JobApplicationService jobApplicationService;
    private final UserService userService;
    private final UserDisplayService userDisplayService;
    private final ApplicationChatWebSocketHub applicationChatWebSocketHub;

    /** WebSocket 握手：校验当前用户是否为该投递的学生或企业方 */
    public void assertParticipant(long applicationId, long userId) {
        requireParticipant(applicationId, userId);
    }

    public PageResponse<ApplicationMessageVO> pageMessages(long applicationId, long viewerUserId, long current, long size) {
        requireParticipant(applicationId, viewerUserId);
        var page = applicationMessageMapper.selectPage(
                new Page<>(current, size),
                new LambdaQueryWrapper<ApplicationMessage>()
                        .eq(ApplicationMessage::getApplicationId, applicationId)
                        .orderByAsc(ApplicationMessage::getCreateTime));
        Page<ApplicationMessageVO> vo = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        vo.setRecords(page.getRecords().stream().map(this::toVo).toList());
        return PageResponse.of(vo);
    }

    @Transactional
    public ApplicationMessageVO sendMessage(long applicationId, long senderUserId, String content) {
        if (!StringUtils.hasText(content)) {
            throw new BusinessException("消息内容不能为空");
        }
        String text = content.trim();
        if (text.length() > MAX_CONTENT) {
            throw new BusinessException("消息过长");
        }
        JobApplication app = requireParticipant(applicationId, senderUserId);
        if (app.getStatus() == ApplicationStatus.CANCELLED) {
            throw new BusinessException("该投递已取消，不可发送消息");
        }
        ApplicationMessage m = new ApplicationMessage();
        m.setApplicationId(applicationId);
        m.setSenderUserId(senderUserId);
        m.setContent(text);
        applicationMessageMapper.insert(m);
        ApplicationMessageVO vo = toVo(m);
        applicationChatWebSocketHub.broadcastNewMessage(applicationId, vo);
        return vo;
    }

    private JobApplication requireParticipant(long applicationId, long userId) {
        JobApplication app = jobApplicationService.getById(applicationId);
        if (app == null) {
            throw new BusinessException(404, "投递不存在");
        }
        if (!Objects.equals(userId, app.getStudentUserId()) && !Objects.equals(userId, app.getCompanyUserId())) {
            throw new BusinessException(403, "无权参与该会话");
        }
        return app;
    }

    private ApplicationMessageVO toVo(ApplicationMessage m) {
        User u = userService.getById(m.getSenderUserId());
        return ApplicationMessageVO.builder()
                .id(m.getId())
                .applicationId(m.getApplicationId())
                .senderUserId(m.getSenderUserId())
                .senderUsername(u != null ? u.getUsername() : null)
                .senderDisplayName(u != null ? userDisplayService.publicDisplayName(u) : null)
                .content(m.getContent())
                .createTime(m.getCreateTime())
                .build();
    }
}
