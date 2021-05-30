package com.syy.security.chapter04.config;

import com.syy.security.chapter04.constants.NormalConstants;
import lombok.Getter;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

import javax.servlet.http.HttpServletRequest;

/**
 * {@link WebAuthenticationDetails#WebAuthenticationDetails(HttpServletRequest)} 设置详情
 *
 * @Description: 优雅的 在security 过滤器链中 实现 验证码校验 2，并获取到 用户的 IP
 * @Author: cuiweiman
 * @Since: 2021/5/28 下午8:41
 */
@Getter
public class MyWebAuthenticationDetails extends WebAuthenticationDetails {

    private boolean isPassed = false;

    /**
     * Records the remote address and will also set the session Id if a session already
     * exists (it won't create one).
     *
     * @param request that the authentication request was received from
     */
    public MyWebAuthenticationDetails(HttpServletRequest request) {
        super(request);
        String code = request.getParameter(NormalConstants.REQUEST_KEY_VERIFY_CODE);
        String verifyCode = (String) request.getSession().getAttribute(NormalConstants.SESSION_KEY_VERIFY_CODE);
        if (StringUtils.isNotBlank(verifyCode) && verifyCode.equalsIgnoreCase(code)) {
            isPassed = true;
        }
    }

}








