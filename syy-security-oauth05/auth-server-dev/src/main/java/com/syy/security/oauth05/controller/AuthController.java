package com.syy.security.oauth05.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description:
 * @Author: cuiweiman
 * @Since: 2021/6/9 下午3:27
 */
@RestController
public class AuthController {

    @GetMapping("auth")
    public String auth() {
        return "auth";
    }
}
