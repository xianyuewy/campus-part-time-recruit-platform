package org.example.col_stu_ptj_sys.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.col_stu_ptj_sys.entity.JobFavorite;
import org.example.col_stu_ptj_sys.mapper.JobFavoriteMapper;
import org.example.col_stu_ptj_sys.service.JobFavoriteService;
import org.springframework.stereotype.Service;

@Service
public class JobFavoriteServiceImpl extends ServiceImpl<JobFavoriteMapper, JobFavorite> implements JobFavoriteService {
}
