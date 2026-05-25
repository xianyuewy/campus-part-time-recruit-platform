package org.example.col_stu_ptj_sys.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("job_favorite")
public class JobFavorite extends com.colstuptjsys.entity.BaseEntity {
    private Long jobId;
    private Long studentUserId;
}
