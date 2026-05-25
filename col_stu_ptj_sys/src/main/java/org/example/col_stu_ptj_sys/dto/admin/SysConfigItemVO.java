package org.example.col_stu_ptj_sys.dto.admin;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SysConfigItemVO {
    private String configKey;
    private String configValue;
    private String remark;
    private LocalDateTime updateTime;
}
