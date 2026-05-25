package org.example.col_stu_ptj_sys.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.example.col_stu_ptj_sys.entity.UserCredit;
import org.example.col_stu_ptj_sys.mapper.UserCreditMapper;
import org.example.col_stu_ptj_sys.service.UserCreditService;
import org.example.col_stu_ptj_sys.support.CreditLevelCalculator;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserCreditServiceImpl extends ServiceImpl<UserCreditMapper, UserCredit> implements UserCreditService {

    @Override
    public void initForNewUser(Long userId) {
        if (lambdaQuery().eq(UserCredit::getUserId, userId).count() > 0) {
            return;
        }
        UserCredit uc = new UserCredit();
        uc.setUserId(userId);
        // 新用户起点：满分 100 分，对应「优秀」档（与业务口径「百分制」一致，勿再硬编码与分数不符的等级）
        int start = 100;
        uc.setScore(start);
        uc.setCreditLevel(CreditLevelCalculator.levelCodeForScore(start));
        save(uc);
    }

    @Override
    public UserCredit getOrCreate(Long userId) {
        UserCredit one = lambdaQuery().eq(UserCredit::getUserId, userId).one();
        if (one == null) {
            initForNewUser(userId);
            one = lambdaQuery().eq(UserCredit::getUserId, userId).one();
        }
        // 新插入行若被库表 DEFAULT 成 NORMAL、或历史脏数据，统一按分数回写
        if (one != null && CreditLevelCalculator.alignScoreAndLevel(one)) {
            updateById(one);
        }
        return one;
    }
}
