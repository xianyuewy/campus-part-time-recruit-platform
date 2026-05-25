package org.example.col_stu_ptj_sys.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {
    /** 可选；传空字符串表示不修改邮箱 */
    @Size(max = 128)
    private String email;

    @Size(max = 20, message = "手机号过长")
    private String phone;

    @Size(max = 64, message = "昵称过长")
    private String nickname;

    @Size(max = 64, message = "真实姓名过长")
    private String realName;
}
