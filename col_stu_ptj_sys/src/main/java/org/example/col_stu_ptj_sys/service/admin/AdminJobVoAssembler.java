package org.example.col_stu_ptj_sys.service.admin;

import lombok.RequiredArgsConstructor;
import org.example.col_stu_ptj_sys.dto.admin.JobAuditVO;
import org.example.col_stu_ptj_sys.entity.CompanyProfile;
import org.example.col_stu_ptj_sys.entity.Job;
import org.example.col_stu_ptj_sys.entity.User;
import org.example.col_stu_ptj_sys.service.CompanyProfileService;
import org.example.col_stu_ptj_sys.service.UserService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 管理端岗位列表/审核：补充发布方企业资料与账号昵称，便于溯源。
 */
@Component
@RequiredArgsConstructor
public class AdminJobVoAssembler {

    private final UserService userService;
    private final CompanyProfileService companyProfileService;

    public JobAuditVO toVo(Job j) {
        Long pid = j.getPublisherUserId();
        User u = pid != null ? userService.getById(pid) : null;
        String companyName = null;
        if (pid != null) {
            CompanyProfile cp = companyProfileService.lambdaQuery()
                    .eq(CompanyProfile::getUserId, pid)
                    .one();
            if (cp != null && StringUtils.hasText(cp.getCompanyName())) {
                companyName = cp.getCompanyName().trim();
            }
        }
        return JobAuditVO.builder()
                .id(j.getId())
                .publisherUserId(pid)
                .publisherUsername(u != null ? u.getUsername() : null)
                .publisherNickname(StringUtils.hasText(u != null ? u.getNickname() : null)
                        ? u.getNickname().trim()
                        : null)
                .publisherCompanyName(companyName)
                .title(j.getTitle())
                .description(j.getDescription())
                .location(j.getLocation())
                .salaryText(j.getSalaryText())
                .status(j.getStatus())
                .auditRemark(j.getAuditRemark())
                .createTime(j.getCreateTime())
                .build();
    }
}
