package org.example.col_stu_ptj_sys.dto.company;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PublishJobRequest {
    @NotBlank
    private String title;
    @NotBlank
    private String description;
    private String location;
    private String jobType;
    private String salaryText;
    private Integer salaryMin;
    private Integer salaryMax;

    @NotBlank(message = "请填写岗位联系电话")
    @Size(max = 32, message = "电话长度不能超过 32 个字符")
    private String contactPhone;

    @NotBlank(message = "请填写微信或沟通方式说明")
    @Size(max = 500, message = "微信说明不能超过 500 字")
    private String contactWechat;

    /** 可选，不填默认发布后 30 天 */
    private LocalDateTime expireAt;
}
