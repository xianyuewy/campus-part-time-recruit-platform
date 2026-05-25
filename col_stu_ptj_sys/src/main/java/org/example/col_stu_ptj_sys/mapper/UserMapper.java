package org.example.col_stu_ptj_sys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.col_stu_ptj_sys.entity.User;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    // 可以在这里添加自定义的SQL方法
}