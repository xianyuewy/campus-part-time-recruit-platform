package org.example.col_stu_ptj_sys.dto.admin;

import lombok.Builder;
import lombok.Data;
import org.example.col_stu_ptj_sys.enums.AuthStatus;
import org.example.col_stu_ptj_sys.enums.UserRole;

import java.time.LocalDateTime;

@Data
@Builder
public class UserAuditVO {
    private Long id;
    private String username;
    private String email;
    private String phone;
    private UserRole role;
    private AuthStatus authStatus;
    private String idCard;
    private String authRemark;
    private LocalDateTime createTime;

    /** 账号是否启用 */
    private Boolean accountEnabled;
}
