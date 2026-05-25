package org.example.col_stu_ptj_sys.service;

import org.example.col_stu_ptj_sys.dto.admin.SysConfigItemVO;
import org.example.col_stu_ptj_sys.entity.SysConfig;
import org.example.col_stu_ptj_sys.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminConfigService {

    private final SysConfigEntityService sysConfigEntityService;

    public List<SysConfigItemVO> listAll() {
        return sysConfigEntityService.list().stream()
                .map(this::toVo)
                .collect(Collectors.toList());
    }

    public SysConfigItemVO getOne(String key) {
        SysConfig c = sysConfigEntityService.getById(key);
        if (c == null) {
            throw new BusinessException(404, "配置不存在");
        }
        return toVo(c);
    }

    @Transactional
    public void upsert(String key, String value, String remark) {
        SysConfig c = sysConfigEntityService.getById(key);
        LocalDateTime now = LocalDateTime.now();
        if (c == null) {
            SysConfig n = new SysConfig();
            n.setConfigKey(key);
            n.setConfigValue(value);
            n.setRemark(remark);
            n.setUpdateTime(now);
            sysConfigEntityService.save(n);
        } else {
            c.setConfigValue(value);
            if (remark != null) {
                c.setRemark(remark);
            }
            c.setUpdateTime(now);
            sysConfigEntityService.updateById(c);
        }
    }

    private SysConfigItemVO toVo(SysConfig c) {
        return SysConfigItemVO.builder()
                .configKey(c.getConfigKey())
                .configValue(c.getConfigValue())
                .remark(c.getRemark())
                .updateTime(c.getUpdateTime())
                .build();
    }
}
