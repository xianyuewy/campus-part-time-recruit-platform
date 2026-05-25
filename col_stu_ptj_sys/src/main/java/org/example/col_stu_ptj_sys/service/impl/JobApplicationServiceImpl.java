package org.example.col_stu_ptj_sys.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.col_stu_ptj_sys.entity.JobApplication;
import org.example.col_stu_ptj_sys.mapper.JobApplicationMapper;
import org.example.col_stu_ptj_sys.service.JobApplicationService;
import org.springframework.stereotype.Service;

@Service
public class JobApplicationServiceImpl extends ServiceImpl<JobApplicationMapper, JobApplication> implements JobApplicationService {
}
