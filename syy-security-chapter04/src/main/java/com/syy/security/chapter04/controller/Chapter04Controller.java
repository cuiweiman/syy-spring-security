package com.syy.security.chapter04.controller;

import com.syy.security.chapter04.config.MyWebAuthenticationDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description: 测试
 * @Author: cuiweiman
 * @Since: 2021/5/28 下午4:42
 */
@Slf4j
@RestController
public class Chapter04Controller {

    @GetMapping("/chapter04")
    public String chapter04() {
        return "chapter04";
    }

    @GetMapping("/authDetail")
    public String hello() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        MyWebAuthenticationDetails details = (MyWebAuthenticationDetails) authentication.getDetails();
        log.info("登陆者信息：{}", details.toString());
        return details.toString();
    }
}
