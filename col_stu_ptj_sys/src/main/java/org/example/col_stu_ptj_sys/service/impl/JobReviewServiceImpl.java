package org.example.col_stu_ptj_sys.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.col_stu_ptj_sys.entity.JobReview;
import org.example.col_stu_ptj_sys.mapper.JobReviewMapper;
import org.example.col_stu_ptj_sys.service.JobReviewService;
import org.springframework.stereotype.Service;

@Service
public class JobReviewServiceImpl extends ServiceImpl<JobReviewMapper, JobReview> implements JobReviewService {
}
