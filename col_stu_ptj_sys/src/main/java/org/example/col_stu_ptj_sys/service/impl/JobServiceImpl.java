package org.example.col_stu_ptj_sys.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.col_stu_ptj_sys.entity.Job;
import org.example.col_stu_ptj_sys.mapper.JobMapper;
import org.example.col_stu_ptj_sys.service.JobService;
import org.springframework.stereotype.Service;

@Service
public class JobServiceImpl extends ServiceImpl<JobMapper, Job> implements JobService {
}
