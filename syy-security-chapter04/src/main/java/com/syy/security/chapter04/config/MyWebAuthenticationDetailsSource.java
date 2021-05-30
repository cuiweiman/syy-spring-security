package com.syy.security.chapter04.config;

import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * @Description: {@link #buildDetails(HttpServletRequest)} 构建 WebAuthenticationDetails： {@link MyWebAuthenticationDetails}
 * 在 {@link MyWebAuthenticationDetails} 中 配置 验证码的校验逻辑，也可以配置其他逻辑，设置或添加其他参数，只需
 * 新增变量即可，然后在 {@link MyAuthenticationProvider }中直接使用。
 * @Author: cuiweiman
 * @Since: 2021/5/28 下午8:48
 */
@Component
public class MyWebAuthenticationDetailsSource implements AuthenticationDetailsSource<HttpServletRequest, MyWebAuthenticationDetails> {

    @Override
    public MyWebAuthenticationDetails buildDetails(HttpServletRequest context) {
        return new MyWebAuthenticationDetails(context);
    }
}
