package org.example.col_stu_ptj_sys.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.col_stu_ptj_sys.entity.SysConfig;
import org.example.col_stu_ptj_sys.mapper.SysConfigMapper;
import org.example.col_stu_ptj_sys.service.SysConfigEntityService;
import org.springframework.stereotype.Service;

@Service
public class SysConfigEntityServiceImpl extends ServiceImpl<SysConfigMapper, SysConfig> implements SysConfigEntityService {
}
