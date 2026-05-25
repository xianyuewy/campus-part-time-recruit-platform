package org.example.col_stu_ptj_sys.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.col_stu_ptj_sys.entity.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Optional;

public interface UserService extends IService<User>, UserDetailsService {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}