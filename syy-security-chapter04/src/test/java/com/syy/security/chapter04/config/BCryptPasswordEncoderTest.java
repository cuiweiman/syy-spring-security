package com.syy.security.chapter04.config;

import com.syy.security.chapter04.SyySecurityChapter04AppTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * @Description: spring security 中 的 BCryptPasswordEncoder 来实现 账户中 密码 的 密文和明文 的转换
 * @Author: cuiweiman
 * @Since: 2021/5/31 下午3:02
 */
@Slf4j
public class BCryptPasswordEncoderTest extends SyySecurityChapter04AppTest {

    private BCryptPasswordEncoder passwordEncoder;

    @Before
    public void init() {
        passwordEncoder = new BCryptPasswordEncoder(10);
    }

    @Test
    public void encode() {
        String password1 = "520";
        String encode1 = passwordEncoder.encode(password1);
        log.info("password 明文：{};  密文：{};", password1, encode1);
        String password2 = "250";
        String encode2 = passwordEncoder.encode(password2);
        log.info("password 明文：{};  密文：{};", password2, encode2);
    }

    public void decode() {

    }

}
