package org.example.col_stu_ptj_sys.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.col_stu_ptj_sys.entity.CompanyProfile;
import org.example.col_stu_ptj_sys.mapper.CompanyProfileMapper;
import org.example.col_stu_ptj_sys.service.CompanyProfileService;
import org.springframework.stereotype.Service;

@Service
public class CompanyProfileServiceImpl extends ServiceImpl<CompanyProfileMapper, CompanyProfile> implements CompanyProfileService {
}
