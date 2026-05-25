package org.example.col_stu_ptj_sys.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.col_stu_ptj_sys.entity.UserCredit;

public interface UserCreditService extends IService<UserCredit> {

    void initForNewUser(Long userId);

    UserCredit getOrCreate(Long userId);
}
