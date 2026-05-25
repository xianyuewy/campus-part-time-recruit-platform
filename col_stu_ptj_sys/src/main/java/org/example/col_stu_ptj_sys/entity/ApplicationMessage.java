package org.example.col_stu_ptj_sys.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("application_message")
public class ApplicationMessage extends com.colstuptjsys.entity.BaseEntity {

    private Long applicationId;
    private Long senderUserId;
    private String content;
}
