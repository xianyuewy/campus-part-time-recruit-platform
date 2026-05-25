package org.example.col_stu_ptj_sys.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("system_notification")
public class SystemNotification extends com.colstuptjsys.entity.BaseEntity {

    private Long userId;
    private String title;
    private String content;
    private String bizType;
    private String bizId;
    private Integer readFlag;
}
