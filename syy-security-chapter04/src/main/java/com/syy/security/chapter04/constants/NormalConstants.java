package com.syy.security.chapter04.constants;

/**
 * @Description: 常用 常量
 * @Author: cuiweiman
 * @Since: 2021/5/26 下午5:46
 */
public class NormalConstants {

    private NormalConstants() {
    }

    /**
     * 前端传递 验证码 的 key
     */
    public static final String REQUEST_KEY_VERIFY_CODE = "code";

    /**
     * 图形验证码 服务端 存储的 key
     */
    public static final String SESSION_KEY_VERIFY_CODE = "verify_code";

    /**
     * 登录 接口
     */
    public static final String SECURITY_LOGIN_URL = "/doLogin";

}
