package org.example.col_stu_ptj_sys.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 争议表单中「关联记录」下拉的候选项
 */
@Data
@Builder
public class DisputeRelationPickVO {
    private Long id;
    /** 下拉展示文案 */
    private String label;
}
