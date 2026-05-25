package org.example.col_stu_ptj_sys.dto.admin;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreditProfileVO {
    private Long userId;
    private String username;
    private Integer score;
    private String creditLevel;
}
