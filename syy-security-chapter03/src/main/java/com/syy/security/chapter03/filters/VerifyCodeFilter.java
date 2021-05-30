package com.syy.security.chapter03.filters;

import com.syy.security.chapter03.constants.NormalConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * {@link GenericFilterBean} 实现这个类：用来配置到 SecurityConfig 中，而不只是 直接过滤 url 请求
 *
 * @Description: 登录接口，图形验证码过滤器
 * @Author: cuiweiman
 * @Since: 2021/5/26 下午5:55
 */
@Slf4j
@Component
public class VerifyCodeFilter extends GenericFilterBean {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        try {
            if (HttpMethod.POST.name().equals(request.getMethod()) && NormalConstants.SECURITY_LOGIN_URL.equals(request.getServletPath())) {
                // 这里直接从 url 上获取了，可以修改成从 header 中获取
                String code = request.getParameter(NormalConstants.REQUEST_KEY_VERIFY_CODE);
                if (StringUtils.isEmpty(code)) {
                    log.error("登录验证码不能为空");
                    throw new AuthenticationServiceException("验证码不能为空");
                }
                String sessionCode = (String) request.getSession().getAttribute(NormalConstants.SESSION_KEY_VERIFY_CODE);
                if (!code.equalsIgnoreCase(sessionCode)) {
                    log.error("登录验证码错误");
                    throw new AuthenticationServiceException("验证码错误");
                } else {
                    filterChain.doFilter(request, response);
                }
            } else {
                filterChain.doFilter(request, response);
            }
        } catch (AuthenticationServiceException | ServletException e) {
            response.setContentType("application/json;charset=utf-8");
            PrintWriter writer = response.getWriter();
            writer.write(e.getMessage());
            writer.flush();
        }
    }
}
