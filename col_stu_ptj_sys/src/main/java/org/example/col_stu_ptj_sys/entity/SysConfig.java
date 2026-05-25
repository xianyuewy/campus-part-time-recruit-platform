package org.example.col_stu_ptj_sys.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统配置（公告、轮播、规则说明等）
 */
@Data
@TableName("sys_config")
public class SysConfig {

    @TableId(type = IdType.INPUT)
    private String configKey;

    private String configValue;

    private String remark;

    private LocalDateTime updateTime;
}
