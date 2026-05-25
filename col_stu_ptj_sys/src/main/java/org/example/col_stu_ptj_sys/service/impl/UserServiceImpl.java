package org.example.col_stu_ptj_sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.col_stu_ptj_sys.entity.User;
import org.example.col_stu_ptj_sys.mapper.UserMapper;
import org.example.col_stu_ptj_sys.service.UserService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("加载用户信息: {}", username);

        User user = findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));

        // 将用户角色转换为Spring Security的权限
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().name());

        boolean enabled = user.getAccountEnabled() == null || user.getAccountEnabled();
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                enabled,
                true,
                true,
                true,
                Collections.singletonList(authority)
        );
    }

    @Override
    public Optional<User> findByUsername(String username) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        User user = getOne(queryWrapper);

        if (user != null) {
            log.debug("找到用户: username={}, id={}", username, user.getId());
        } else {
            log.debug("未找到用户: username={}", username);
        }

        return Optional.ofNullable(user);
    }

    @Override
    public boolean existsByUsername(String username) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        long count = count(queryWrapper);
        log.debug("检查用户名是否存在: username={}, exists={}", username, count > 0);
        return count > 0;
    }

    @Override
    public boolean existsByEmail(String email) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", email);
        long count = count(queryWrapper);
        log.debug("检查邮箱是否存在: email={}, exists={}", email, count > 0);
        return count > 0;
    }
}