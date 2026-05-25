package org.example.col_stu_ptj_sys.service;

import lombok.RequiredArgsConstructor;
import org.example.col_stu_ptj_sys.dto.PublicConfigVO;
import org.example.col_stu_ptj_sys.entity.SysConfig;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PublicConfigService {

    private final SysConfigEntityService sysConfigEntityService;

    public PublicConfigVO getPublicConfig() {
        String announcement = "";
        String banner = "[]";
        String rules = "";
        SysConfig a = sysConfigEntityService.getById("announcement");
        if (a != null && a.getConfigValue() != null) {
            announcement = a.getConfigValue();
        }
        SysConfig b = sysConfigEntityService.getById("banner_images");
        if (b != null && b.getConfigValue() != null) {
            banner = b.getConfigValue();
        }
        SysConfig c = sysConfigEntityService.getById("credit_rules");
        if (c != null && c.getConfigValue() != null) {
            rules = c.getConfigValue();
        }
        return PublicConfigVO.builder()
                .announcement(announcement)
                .bannerImagesJson(banner)
                .creditRules(rules)
                .build();
    }
}
