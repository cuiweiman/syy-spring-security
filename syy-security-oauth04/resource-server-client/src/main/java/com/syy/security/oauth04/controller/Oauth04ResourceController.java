package com.syy.security.oauth04.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description:
 * @Author: cuiweiman
 * @Since: 2021/6/3 上午11:38
 */
@RestController
public class Oauth04ResourceController {

    @GetMapping("/hello")
    public String hello() {
        return "hello";
    }

    @GetMapping("/resource")
    public String resource() {
        return "resource";
    }

    @GetMapping("/admin/hello")
    public String admin() {
        return "admin";
    }
}
