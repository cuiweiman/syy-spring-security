package com.syy.security.chapter04.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description: 测试
 * @Author: cuiweiman
 * @Since: 2021/5/28 下午4:42
 */
@RestController
public class Chapter04Controller {

    @GetMapping("/chapter04")
    public String chapter04() {
        return "chapter04";
    }

}
