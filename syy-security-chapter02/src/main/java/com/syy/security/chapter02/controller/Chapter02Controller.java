package com.syy.security.chapter02.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description: test
 * @Author: cuiweiman
 * @Since: 2021/5/25 下午8:06
 */
@RestController
public class Chapter02Controller {

    @GetMapping("/chapter02")
    public String chapter02() {
        return "chapter02";
    }

    /**
     * myGirl 用户可以访问
     *
     * @return 结果
     */
    @GetMapping("/myGirl/sing")
    public String myGirl() {
        return "singing";
    }

    /**
     * me 角色具有全部权限
     *
     * @return 结果
     */
    @GetMapping("/me/anything")
    public String me() {
        return "doAnything";
    }

}
