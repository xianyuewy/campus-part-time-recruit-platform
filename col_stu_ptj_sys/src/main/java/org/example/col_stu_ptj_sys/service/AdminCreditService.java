package org.example.col_stu_ptj_sys.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.col_stu_ptj_sys.dto.PageResponse;
import org.example.col_stu_ptj_sys.dto.admin.CreditDisputeVO;
import org.example.col_stu_ptj_sys.dto.admin.CreditProfileVO;
import org.example.col_stu_ptj_sys.entity.CreditAdjustLog;
import org.example.col_stu_ptj_sys.entity.CreditDispute;
import org.example.col_stu_ptj_sys.entity.User;
import org.example.col_stu_ptj_sys.entity.UserCredit;
import org.example.col_stu_ptj_sys.support.CreditLevelCalculator;
import org.example.col_stu_ptj_sys.enums.DisputeStatus;
import org.example.col_stu_ptj_sys.enums.UserRole;
import org.example.col_stu_ptj_sys.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminCreditService {

    private final ObjectMapper objectMapper;
    private final UserCreditService userCreditService;
    private final UserService userService;
    private final UserDisplayService userDisplayService;
    private final CreditAdjustLogService creditAdjustLogService;
    private final CreditDisputeService creditDisputeService;

    public PageResponse<CreditProfileVO> pageProfiles(long current, long size, String keyword) {
        Page<User> page = new Page<>(current, size);
        var q = userService.lambdaQuery()
                .in(User::getRole, UserRole.STUDENT, UserRole.COMPANY);
        if (StringUtils.hasText(keyword)) {
            q.and(w -> w.like(User::getUsername, keyword));
        }
        q.orderByDesc(User::getCreateTime);
        Page<User> result = q.page(page);
        Page<CreditProfileVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(u -> {
            UserCredit uc = userCreditService.getOrCreate(u.getId());
            // 等级必须以分数为准展示，不直接使用库表 credit_level（可能曾为默认 NORMAL 且未写回成功）
            int sc = CreditLevelCalculator.clampScore(uc.getScore());
            String level = CreditLevelCalculator.levelCodeForScore(sc);
            return CreditProfileVO.builder()
                    .userId(u.getId())
                    .username(u.getUsername())
                    .score(sc)
                    .creditLevel(level)
                    .build();
        }).toList());
        return PageResponse.of(voPage);
    }

    @Transactional
    public void adjustScore(Long userId, int delta, String reason) {
        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        if (delta == 0) {
            throw new BusinessException("调整分不能为0");
        }
        UserCredit uc = userCreditService.getOrCreate(userId);
        int next = CreditLevelCalculator.clampScore(uc.getScore() + delta);
        uc.setScore(next);
        uc.setCreditLevel(CreditLevelCalculator.levelCodeForScore(next));
        userCreditService.updateById(uc);

        String admin = SecurityContextHolder.getContext().getAuthentication().getName();
        CreditAdjustLog log = new CreditAdjustLog();
        log.setUserId(userId);
        log.setDelta(delta);
        log.setReason(reason);
        log.setAdminUsername(admin != null ? admin : "unknown");
        creditAdjustLogService.save(log);
    }

    public PageResponse<CreditDisputeVO> pageDisputes(long current, long size, String status) {
        Page<CreditDispute> page = new Page<>(current, size);
        var q = creditDisputeService.lambdaQuery();
        if (StringUtils.hasText(status)) {
            q.eq(CreditDispute::getStatus, DisputeStatus.valueOf(status.toUpperCase()));
        }
        q.orderByDesc(CreditDispute::getCreateTime);
        Page<CreditDispute> result = q.page(page);
        Page<CreditDisputeVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(this::toDisputeVo).toList());
        return PageResponse.of(voPage);
    }

    @Transactional
    public void resolveDispute(Long id, String action, String adminRemark) {
        CreditDispute d = creditDisputeService.getById(id);
        if (d == null) {
            throw new BusinessException(404, "工单不存在");
        }
        if (d.getStatus() != DisputeStatus.PENDING) {
            throw new BusinessException("工单已处理");
        }
        if ("RESOLVED".equalsIgnoreCase(action)) {
            d.setStatus(DisputeStatus.RESOLVED);
        } else if ("REJECTED".equalsIgnoreCase(action)) {
            d.setStatus(DisputeStatus.REJECTED);
        } else {
            throw new BusinessException("无效操作");
        }
        d.setAdminRemark(adminRemark);
        d.setResolvedAt(LocalDateTime.now());
        creditDisputeService.updateById(d);
    }

    private CreditDisputeVO toDisputeVo(CreditDispute d) {
        User reporter = userService.getById(d.getReporterUserId());
        User target = userService.getById(d.getTargetUserId());
        return CreditDisputeVO.builder()
                .id(d.getId())
                .reporterUserId(d.getReporterUserId())
                .reporterUsername(reporter != null ? reporter.getUsername() : "?")
                .reporterDisplayName(reporter != null ? userDisplayService.publicDisplayName(reporter) : null)
                .targetUserId(d.getTargetUserId())
                .targetUsername(target != null ? target.getUsername() : "?")
                .targetDisplayName(target != null ? userDisplayService.publicDisplayName(target) : null)
                .relatedType(d.getRelatedType())
                .relatedId(d.getRelatedId())
                .reason(d.getReason())
                .supplementText(d.getSupplementText())
                .evidenceUrls(parseEvidenceUrls(d.getEvidenceUrls()))
                .status(d.getStatus())
                .adminRemark(d.getAdminRemark())
                .resolvedAt(d.getResolvedAt())
                .createTime(d.getCreateTime())
                .build();
    }

    private List<String> parseEvidenceUrls(String json) {
        if (!StringUtils.hasText(json)) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

}
