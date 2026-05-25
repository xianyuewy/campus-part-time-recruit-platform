package org.example.col_stu_ptj_sys.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 前台可见的平台配置（公告、轮播、规则）
 */
@Data
@Builder
public class PublicConfigVO {
    private String announcement;
    private String bannerImagesJson;
    private String creditRules;
}
