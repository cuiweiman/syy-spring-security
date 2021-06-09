package com.syy.security.oauth05.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description: 资源服务
 * @Author: cuiweiman
 * @Since: 2021/6/9 下午1:53
 */
@RestController
public class Oauth05ResourceApp {

    @GetMapping("/hello")
    public String hello() {
        return "hello";
    }

    @GetMapping("/admin/hello")
    public String admin() {
        return "admin";
    }

}
