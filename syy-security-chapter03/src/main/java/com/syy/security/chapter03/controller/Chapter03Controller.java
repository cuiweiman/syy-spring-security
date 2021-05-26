package com.syy.security.chapter03.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 角色 me > myGirl
 *
 * @Description: 基础测试 controller
 * @Author: cuiweiman
 * @Since: 2021/5/26 上午11:53
 */
@RestController
public class Chapter03Controller {

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

    /**
     * 获取当前 登陆 的用户信息：
     * https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247488050&idx=1&sn=3cea9d8eb13d7bda1407b111e5c8ee45
     *
     * @param authentication 对象会被自动注入
     * @return 当前用户信息
     */
    @GetMapping("/info")
    public String getCurrentUserInfo(Authentication authentication) {
        return authentication.getPrincipal().toString();
    }

}
