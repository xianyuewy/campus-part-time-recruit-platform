package org.example.col_stu_ptj_sys.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.col_stu_ptj_sys.entity.SystemNotification;
import org.example.col_stu_ptj_sys.mapper.SystemNotificationMapper;
import org.example.col_stu_ptj_sys.service.SystemNotificationService;
import org.springframework.stereotype.Service;

@Service
public class SystemNotificationServiceImpl extends ServiceImpl<SystemNotificationMapper, SystemNotification>
        implements SystemNotificationService {
}
