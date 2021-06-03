package com.syy.security.oauth01.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description:
 * @Author: cuiweiman
 * @Since: 2021/6/2 下午8:36
 */
@RequestMapping("/oauth01")
@RestController
public class OAuth01Controller {

    @GetMapping("/test")
    public String test() {
        return "oauth 01 ";
    }

}
