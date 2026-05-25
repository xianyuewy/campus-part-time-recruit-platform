package org.example.col_stu_ptj_sys.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.col_stu_ptj_sys.config.UploadPathService;
import org.example.col_stu_ptj_sys.dto.CreateCreditDisputeRequest;
import org.example.col_stu_ptj_sys.dto.CreditDisputeSimpleVO;
import org.example.col_stu_ptj_sys.dto.DisputeCounterpartyVO;
import org.example.col_stu_ptj_sys.dto.DisputeRelationPickVO;
import org.example.col_stu_ptj_sys.dto.PageResponse;
import org.example.col_stu_ptj_sys.entity.CreditAdjustLog;
import org.example.col_stu_ptj_sys.entity.CreditDispute;
import org.example.col_stu_ptj_sys.entity.Job;
import org.example.col_stu_ptj_sys.entity.JobApplication;
import org.example.col_stu_ptj_sys.entity.JobReview;
import org.example.col_stu_ptj_sys.entity.User;
import org.example.col_stu_ptj_sys.entity.UserCredit;
import org.example.col_stu_ptj_sys.enums.CreditRelatedType;
import org.example.col_stu_ptj_sys.enums.DisputeStatus;
import org.example.col_stu_ptj_sys.enums.UserRole;
import org.example.col_stu_ptj_sys.exception.BusinessException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserCreditPortalService {

    private static final Set<String> EVIDENCE_EXT = Set.of(".jpg", ".jpeg", ".png", ".gif", ".webp", ".pdf");

    private final ObjectMapper objectMapper;
    private final UserService userService;
    private final UserCreditService userCreditService;
    private final CreditAdjustLogService creditAdjustLogService;
    private final CreditDisputeService creditDisputeService;
    private final JobApplicationService jobApplicationService;
    private final JobService jobService;
    private final UserDisplayService userDisplayService;
    private final JobReviewService jobReviewService;
    private final UploadPathService uploadPathService;

    public UserCredit myCredit(UserRole requiredRole) {
        User me = requireRole(requiredRole);
        return userCreditService.getOrCreate(me.getId());
    }

    public PageResponse<CreditAdjustLog> myAdjustLogs(UserRole requiredRole, long current, long size) {
        User me = requireRole(requiredRole);
        var page = creditAdjustLogService.lambdaQuery()
                .eq(CreditAdjustLog::getUserId, me.getId())
                .orderByDesc(CreditAdjustLog::getCreateTime)
                .page(new Page<>(current, size));
        return PageResponse.of(page);
    }

    /**
     * @param scope INITIATED=我发起的；RECEIVED=针对我的（他人诉我）；ALL=两者
     */
    public PageResponse<CreditDisputeSimpleVO> myDisputes(UserRole requiredRole, long current, long size, String scope) {
        User me = requireRole(requiredRole);
        String s = (scope == null || scope.isBlank()) ? "INITIATED" : scope.trim().toUpperCase();
        if (!List.of("INITIATED", "RECEIVED", "ALL").contains(s)) {
            s = "INITIATED";
        }
        var q = creditDisputeService.lambdaQuery();
        switch (s) {
            case "RECEIVED" -> q.eq(CreditDispute::getTargetUserId, me.getId());
            case "ALL" -> q.and(w -> w.eq(CreditDispute::getReporterUserId, me.getId())
                    .or()
                    .eq(CreditDispute::getTargetUserId, me.getId()));
            default -> q.eq(CreditDispute::getReporterUserId, me.getId());
        }
        var page = q.orderByDesc(CreditDispute::getCreateTime).page(new Page<>(current, size));
        Page<CreditDisputeSimpleVO> vo = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        vo.setRecords(page.getRecords().stream().map(this::toDisputeSimpleVo).toList());
        return PageResponse.of(vo);
    }

    private CreditDisputeSimpleVO toDisputeSimpleVo(CreditDispute d) {
        User reporter = userService.getById(d.getReporterUserId());
        User target = userService.getById(d.getTargetUserId());
        return CreditDisputeSimpleVO.builder()
                .id(d.getId())
                .reporterUserId(d.getReporterUserId())
                .reporterUsername(reporter != null ? reporter.getUsername() : null)
                .reporterDisplayName(reporter != null ? userDisplayService.publicDisplayName(reporter) : null)
                .targetUserId(d.getTargetUserId())
                .targetUsername(target != null ? target.getUsername() : null)
                .targetDisplayName(target != null ? userDisplayService.publicDisplayName(target) : null)
                .relatedType(d.getRelatedType())
                .relatedId(d.getRelatedId())
                .reason(d.getReason())
                .supplementText(d.getSupplementText())
                .evidenceUrls(parseEvidenceUrlsJson(d.getEvidenceUrls()))
                .status(d.getStatus())
                .adminRemark(d.getAdminRemark())
                .createTime(d.getCreateTime())
                .build();
    }

    private List<String> parseEvidenceUrlsJson(String json) {
        if (!StringUtils.hasText(json)) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return List.of();
        }
    }

    /**
     * 申诉人在待处理阶段补充文字说明与证据文件（可多次提交，证据追加存储）
     */
    @Transactional
    public void submitDisputeSupplement(UserRole requiredRole, Long disputeId, String supplementText,
                                        List<MultipartFile> files) {
        User me = requireRole(requiredRole);
        CreditDispute d = creditDisputeService.getById(disputeId);
        if (d == null) {
            throw new BusinessException(404, "工单不存在");
        }
        if (!me.getId().equals(d.getReporterUserId())) {
            throw new BusinessException(403, "仅申诉人可补充材料");
        }
        if (d.getStatus() != DisputeStatus.PENDING) {
            throw new BusinessException("工单已处理，不可再补充");
        }
        if (StringUtils.hasText(supplementText)) {
            d.setSupplementText(supplementText.trim());
        }
        List<String> urls = new ArrayList<>(parseEvidenceUrlsJson(d.getEvidenceUrls()));
        if (files != null) {
            for (MultipartFile f : files) {
                if (f == null || f.isEmpty()) {
                    continue;
                }
                try {
                    urls.add(storeDisputeEvidence(disputeId, f));
                } catch (IOException e) {
                    throw new BusinessException("保存证据文件失败");
                }
            }
        }
        if (!urls.isEmpty()) {
            try {
                d.setEvidenceUrls(objectMapper.writeValueAsString(urls));
            } catch (JsonProcessingException e) {
                throw new BusinessException("保存失败");
            }
        }
        creditDisputeService.updateById(d);
    }

    private String storeDisputeEvidence(Long disputeId, MultipartFile f) throws IOException {
        if (f.getSize() > 5 * 1024 * 1024) {
            throw new BusinessException("单个证据文件不能超过 5MB");
        }
        String orig = Objects.requireNonNullElse(f.getOriginalFilename(), "file");
        String ext = extOfFilename(orig).toLowerCase(Locale.ROOT);
        if (!EVIDENCE_EXT.contains(ext)) {
            throw new BusinessException("证据仅支持 jpg/png/gif/webp/pdf");
        }
        String ct = f.getContentType();
        if (ct != null) {
            String c = ct.toLowerCase(Locale.ROOT);
            if (!c.startsWith("image/") && !c.contains("pdf")) {
                throw new BusinessException("文件类型不支持");
            }
        }
        String fn = UUID.randomUUID().toString().replace("-", "") + ext;
        Path dir = uploadPathService.resolve("dispute", String.valueOf(disputeId));
        Files.createDirectories(dir);
        Path target = dir.resolve(fn);
        f.transferTo(target.toFile());
        return "/uploads/dispute/" + disputeId + "/" + fn;
    }

    private static String extOfFilename(String filename) {
        int i = filename.lastIndexOf('.');
        return i >= 0 ? filename.substring(i) : "";
    }

    /**
     * 仅展示与当前用户有过岗位申请往来的一方，用于争议对象下拉（无需记忆用户 ID）
     */
    public List<DisputeCounterpartyVO> listDisputeCounterparties(UserRole requiredRole) {
        User me = requireRole(requiredRole);
        List<JobApplication> apps = jobApplicationService.lambdaQuery()
                .eq(requiredRole == UserRole.STUDENT ? JobApplication::getStudentUserId : JobApplication::getCompanyUserId, me.getId())
                .orderByDesc(JobApplication::getCreateTime)
                .list();
        Set<Long> seen = new LinkedHashSet<>();
        List<DisputeCounterpartyVO> out = new ArrayList<>();
        for (JobApplication app : apps) {
            Long otherId = requiredRole == UserRole.STUDENT ? app.getCompanyUserId() : app.getStudentUserId();
            if (otherId == null || !seen.add(otherId)) {
                continue;
            }
            User u = userService.getById(otherId);
            String displayName = u != null ? userDisplayService.publicDisplayName(u) : "用户";
            String hint;
            Job job = jobService.getById(app.getJobId());
            hint = "最近往来岗位：" + (job != null ? job.getTitle() : "—");
            out.add(DisputeCounterpartyVO.builder()
                    .userId(otherId)
                    .username(u != null ? u.getUsername() : null)
                    .displayName(displayName)
                    .hint(hint)
                    .build());
        }
        return out;
    }

    /**
     * 根据对方用户与关联类型，列出可选的具体业务主键（申请 / 岗位 / 评价）
     */
    public List<DisputeRelationPickVO> listDisputeRelationPicks(UserRole requiredRole, long targetUserId, String relatedTypeRaw) {
        User me = requireRole(requiredRole);
        assertHasHiringHistory(me.getId(), targetUserId, requiredRole);
        CreditRelatedType type;
        try {
            type = CreditRelatedType.fromCode(relatedTypeRaw);
        } catch (IllegalArgumentException e) {
            throw new BusinessException("不支持的关联类型");
        }
        if (type == CreditRelatedType.NONE) {
            return List.of();
        }
        return switch (type) {
            case APPLICATION -> pickApplications(me.getId(), targetUserId, requiredRole);
            case JOB -> pickJobs(me.getId(), targetUserId, requiredRole);
            case REVIEW -> pickReviews(me.getId(), targetUserId);
            default -> List.of();
        };
    }

    @Transactional
    public void submitDispute(UserRole requiredRole, CreateCreditDisputeRequest req) {
        User me = requireRole(requiredRole);
        if (me.getId().equals(req.getTargetUserId())) {
            throw new BusinessException("不能对自己发起争议");
        }
        User target = userService.getById(req.getTargetUserId());
        if (target == null) {
            throw new BusinessException(404, "目标用户不存在");
        }
        assertHasHiringHistory(me.getId(), req.getTargetUserId(), requiredRole);

        CreditRelatedType rel;
        try {
            rel = CreditRelatedType.fromCode(req.getRelatedType());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("不支持的关联类型");
        }

        CreditDispute d = new CreditDispute();
        d.setReporterUserId(me.getId());
        d.setTargetUserId(req.getTargetUserId());
        d.setReason(req.getReason().trim());
        d.setStatus(DisputeStatus.PENDING);

        if (rel == CreditRelatedType.NONE) {
            d.setRelatedType(null);
            d.setRelatedId(null);
        } else {
            if (req.getRelatedId() == null) {
                throw new BusinessException("请选择要关联的具体记录");
            }
            validateRelatedEvidence(requiredRole, me, req.getTargetUserId(), rel, req.getRelatedId());
            d.setRelatedType(rel.getCode());
            d.setRelatedId(req.getRelatedId());
        }

        creditDisputeService.save(d);
    }

    private void assertHasHiringHistory(long myId, long targetId, UserRole myRole) {
        long c;
        if (myRole == UserRole.STUDENT) {
            c = jobApplicationService.lambdaQuery()
                    .eq(JobApplication::getStudentUserId, myId)
                    .eq(JobApplication::getCompanyUserId, targetId)
                    .count();
        } else {
            c = jobApplicationService.lambdaQuery()
                    .eq(JobApplication::getCompanyUserId, myId)
                    .eq(JobApplication::getStudentUserId, targetId)
                    .count();
        }
        if (c == 0) {
            throw new BusinessException("只能选择与您有过投递或招聘往来的用户");
        }
    }

    private void validateRelatedEvidence(UserRole role, User me, long targetId, CreditRelatedType type, long relatedId) {
        switch (type) {
            case APPLICATION -> {
                JobApplication app = jobApplicationService.getById(relatedId);
                if (app == null) {
                    throw new BusinessException(404, "申请记录不存在");
                }
                boolean ok = (me.getId().equals(app.getStudentUserId()) && targetId == app.getCompanyUserId())
                        || (me.getId().equals(app.getCompanyUserId()) && targetId == app.getStudentUserId());
                if (!ok) {
                    throw new BusinessException("该申请记录与争议双方不匹配");
                }
            }
            case JOB -> {
                Job job = jobService.getById(relatedId);
                if (job == null) {
                    throw new BusinessException(404, "岗位不存在");
                }
                long cnt = jobApplicationService.lambdaQuery()
                        .eq(JobApplication::getJobId, relatedId)
                        .and(w -> w.eq(JobApplication::getStudentUserId, me.getId()).eq(JobApplication::getCompanyUserId, targetId)
                                .or(w2 -> w2.eq(JobApplication::getStudentUserId, targetId).eq(JobApplication::getCompanyUserId, me.getId())))
                        .count();
                if (cnt == 0) {
                    throw new BusinessException("该岗位与双方往来记录不一致");
                }
            }
            case REVIEW -> {
                JobReview r = jobReviewService.getById(relatedId);
                if (r == null) {
                    throw new BusinessException(404, "评价记录不存在");
                }
                boolean pair = (me.getId().equals(r.getFromUserId()) && targetId == r.getToUserId())
                        || (me.getId().equals(r.getToUserId()) && targetId == r.getFromUserId());
                if (!pair) {
                    throw new BusinessException("该评价记录与争议双方不匹配");
                }
            }
            default -> {
            }
        }
    }

    private List<DisputeRelationPickVO> pickApplications(long myId, long targetId, UserRole role) {
        var q = jobApplicationService.lambdaQuery();
        if (role == UserRole.STUDENT) {
            q.eq(JobApplication::getStudentUserId, myId).eq(JobApplication::getCompanyUserId, targetId);
        } else {
            q.eq(JobApplication::getCompanyUserId, myId).eq(JobApplication::getStudentUserId, targetId);
        }
        List<JobApplication> apps = q.orderByDesc(JobApplication::getCreateTime).list();
        return apps.stream().map(app -> {
            Job job = jobService.getById(app.getJobId());
            String jt = job != null ? job.getTitle() : "?";
            String label = String.format("申请 #%d ·《%s》· %s · %s",
                    app.getId(), jt, app.getStatus(), app.getCreateTime());
            return DisputeRelationPickVO.builder().id(app.getId()).label(label).build();
        }).toList();
    }

    private List<DisputeRelationPickVO> pickJobs(long myId, long targetId, UserRole role) {
        var q = jobApplicationService.lambdaQuery();
        if (role == UserRole.STUDENT) {
            q.eq(JobApplication::getStudentUserId, myId).eq(JobApplication::getCompanyUserId, targetId);
        } else {
            q.eq(JobApplication::getCompanyUserId, myId).eq(JobApplication::getStudentUserId, targetId);
        }
        List<JobApplication> apps = q.orderByDesc(JobApplication::getCreateTime).list();
        Set<Long> jobIds = apps.stream().map(JobApplication::getJobId).collect(Collectors.toCollection(LinkedHashSet::new));
        List<DisputeRelationPickVO> out = new ArrayList<>();
        for (Long jid : jobIds) {
            Job job = jobService.getById(jid);
            if (job == null) {
                continue;
            }
            out.add(DisputeRelationPickVO.builder()
                    .id(jid)
                    .label(String.format("岗位 #%d · %s", jid, job.getTitle()))
                    .build());
        }
        return out;
    }

    private List<DisputeRelationPickVO> pickReviews(long myId, long targetId) {
        List<JobReview> revs = jobReviewService.lambdaQuery()
                .and(w -> w.eq(JobReview::getFromUserId, myId).eq(JobReview::getToUserId, targetId))
                .or(w -> w.eq(JobReview::getFromUserId, targetId).eq(JobReview::getToUserId, myId))
                .orderByDesc(JobReview::getCreateTime)
                .list();
        return revs.stream().map(r -> DisputeRelationPickVO.builder()
                .id(r.getId())
                .label(String.format("评价 #%d · 评分 %d · %s", r.getId(), r.getScore(), r.getCreateTime()))
                .build()).toList();
    }

    private User requireRole(UserRole requiredRole) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.findByUsername(username).orElseThrow(() -> new BusinessException("用户不存在"));
        if (user.getRole() != requiredRole) {
            throw new BusinessException(403, "无权访问");
        }
        return user;
    }
}
