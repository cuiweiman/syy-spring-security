package com.syy.security.chapter04.config;

import com.syy.security.chapter04.constants.NormalConstants;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * 优雅的实现 在登陆过程中，进行 验证码校验；避免 {@link com.syy.security.chapter03.filters.VerifyCodeFilter#doFilter)}
 * 自定义过滤器 的低效。
 * <p>
 * 实现 校验验证码的思路：
 * 登录请求是调用 {@link AbstractUserDetailsAuthenticationProvider#authenticate} 方法进行认证的，在该方法中，又会调用到
 * {@link  DaoAuthenticationProvider#additionalAuthenticationChecks} 方法做进一步的校验，去校验用户登录密码。我们可以自定义一
 * 个 AuthenticationProvider 代替 DaoAuthenticationProvider，并重写其 additionalAuthenticationChecks 方法，加入验证码的校验逻辑即可。
 * <p>
 * 1. 首先获取当前请求，注意这种获取方式，在基于 Spring 的 web 项目中，我们可以随时随地获取到当前请求
 * 2. 从当前请求中拿到 code 参数，也就是用户传来的验证码。
 * 3. 从 session 中获取生成的验证码字符串。
 * 4. 两者进行比较，如果验证码输入错误，则直接抛出异常。
 * 5. 最后通过 super 调用父类方法，也就是 DaoAuthenticationProvider 的 additionalAuthenticationChecks 方法，该方法中主要做密码的校验。
 * 6. MyAuthenticationProvider 定义好之后，接下来主要是如何让 MyAuthenticationProvider 代替 DaoAuthenticationProvider。
 *
 * @Description: 校验请求中的 验证码
 * @Author: cuiweiman
 * @Since: 2021/5/28 下午5:26
 */
public class MyAuthenticationProvider extends DaoAuthenticationProvider {

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
        HttpServletRequest req = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String code = req.getParameter(NormalConstants.REQUEST_KEY_VERIFY_CODE);
        String verifyCode = (String) req.getSession().getAttribute(NormalConstants.SESSION_KEY_VERIFY_CODE);
        if (StringUtils.isBlank(code) || !code.equals(verifyCode)) {
            throw new AuthenticationServiceException("验证码错误");
        }
        super.additionalAuthenticationChecks(userDetails, authentication);
    }
}
