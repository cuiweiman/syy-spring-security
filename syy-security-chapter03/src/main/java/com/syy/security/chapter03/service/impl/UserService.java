package com.syy.security.chapter03.service.impl;

import com.syy.security.chapter03.model.domain.UserDO;
import com.syy.security.chapter03.model.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @Description:
 * @Author: cuiweiman
 * @Since: 2021/5/26 下午1:38
 */
@Slf4j
@Service
public class UserService implements UserDetailsService {

    @Resource
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserDO userDO = userRepository.findUserByUsername(username);
        if (Objects.isNull(userDO)) {
            log.info("用户不存在，其用户名为：{} ", username);
            throw new UsernameNotFoundException(username + " 用户不存在！");
        }
        return userDO;
    }
}
