package org.example.col_stu_ptj_sys.dto.admin;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DailyTrendPoint {
    private String date;
    private long newUsers;
    private long newJobs;
}
