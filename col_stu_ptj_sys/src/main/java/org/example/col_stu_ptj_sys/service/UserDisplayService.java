package org.example.col_stu_ptj_sys.service;

import lombok.RequiredArgsConstructor;
import org.example.col_stu_ptj_sys.entity.CompanyProfile;
import org.example.col_stu_ptj_sys.entity.User;
import org.example.col_stu_ptj_sys.enums.UserRole;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 全站用户对外展示名：企业用户优先用企业资料中的企业名称，否则昵称，否则登录账号。
 * 学生/管理员为昵称，否则登录账号。真实姓名为 {@link User#getRealName()}，用于补充信息、后台核对，不作为默认外显。
 */
@Service
@RequiredArgsConstructor
public class UserDisplayService {

    private final CompanyProfileService companyProfileService;

    public String publicDisplayName(User u) {
        if (u == null) {
            return null;
        }
        if (u.getRole() == UserRole.COMPANY) {
            CompanyProfile cp = companyProfileService.lambdaQuery()
                    .eq(CompanyProfile::getUserId, u.getId())
                    .one();
            if (cp != null && StringUtils.hasText(cp.getCompanyName())) {
                return cp.getCompanyName().trim();
            }
        }
        if (StringUtils.hasText(u.getNickname())) {
            return u.getNickname().trim();
        }
        return u.getUsername();
    }
}
