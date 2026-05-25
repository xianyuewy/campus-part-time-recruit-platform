package org.example.col_stu_ptj_sys.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("company_profile")
public class CompanyProfile extends com.colstuptjsys.entity.BaseEntity {
    private Long userId;
    private String companyName;
    private String licenseNo;
    private String contactName;
    private String contactPhone;
    @TableField("description")
    private String description;
}
