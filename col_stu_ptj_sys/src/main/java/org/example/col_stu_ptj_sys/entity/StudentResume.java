package org.example.col_stu_ptj_sys.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("student_resume")
public class StudentResume extends com.colstuptjsys.entity.BaseEntity {

    private Long userId;
    private String selfIntro;
    private String education;
    private String skills;
    private String workExperience;
    /** 简历附件访问路径，如 /uploads/resumes/xxx.pdf */
    private String attachmentPath;
    /** 附件原始文件名（展示/下载用） */
    private String attachmentOriginalName;
}
