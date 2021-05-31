
### chapter04
1. AuthenticationProvider 定义了 Spring Security 中的验证逻辑
    1. authenticate 方法用来做验证，即验证用户身份
    2. supports 则用来判断当前的 AuthenticationProvider 是否支持对应的 Authentication。

2. SpringSecurity 重要的接口 Authentication
    1. getAuthorities 方法用来获取用户的权限。
    2. getCredentials 方法用来获取用户凭证，一般来说就是【密码】。DaoAuthenticationProvider#additionalAuthenticationChecks 密码校验
    3. getDetails 方法用来获取用户携带的详细信息，可能是当前请求之类的东西。
    4. getPrincipal 方法用来获取当前用户，可能是一个用户名，也可能是一个用户对象。
    5. isAuthenticated 当前用户是否认证成功。

    > 最常用的 实现类： UsernamePasswordAuthenticationToken


### 自定义认证思路
> 在 chapter03 的 验证码过滤器实现过程中，自定义的过滤器加入到了 Spring Security 的过滤器链中，进而实现了添加登录验证码功能；
但是这种方式是有弊端的，破坏了原有的过滤器链，请求每次都要走一遍验证码过滤器，这样不合理。

登录请求是调用 AbstractUserDetailsAuthenticationProvider#authenticate 方法进行认证的，在该方法中，又会调用到
DaoAuthenticationProvider#additionalAuthenticationChecks 方法做进一步的校验，去校验用户登录密码。
我们可以自定义一个 AuthenticationProvider 代替 DaoAuthenticationProvider，并重写它里边的 additionalAuthenticationChecks 方法，
在重写的过程中，加入验证码的校验逻辑即可。这样既不破坏原有的过滤器链，又实现了自定义认证功能。
「常见的手机号码动态登录，也可以使用这种方式来认证。」

### 踢掉前一个账号登录
#### 简单配置 最大 session 即可
```java
// 在 configure 中添加配置：
// 关闭 csrf 功能
.and().csrf().disable()
// 设置 客户端最大登录次数 为 1，超过次数后 默认会直接 踢掉 最先登录的客户端
.sessionManagement()
.maximumSessions(1)
// 客户端登录 达到最大次数后，禁止新的客户端登录
.maxSessionsPreventsLogin(true);

// 需要添加新的 Bean 来监听和发布 客户端 session 的创建和销毁事件
@Bean
protected HttpSessionEventPublisher httpSessionEventPublisher() {
    return new HttpSessionEventPublisher();
}
```

- sessionManagement().maximumSessions(1)：控制 只允许一个客户端登录，再有新的客户端登录时，会踢掉上一个登录用户；
- maxSessionsPreventsLogin(true)：配置 不会踢掉上一个登录用户，而是直接 禁止 新的客户端 登录。

> 为什么要加 HttpSessionEventPublisher 这个 Bean 呢？因为在 Spring Security 中，它是通过监听 session 的销毁事件，来及时的清理 session 的记录。
用户从不同的浏览器登录后，都会有对应的 session，当用户注销登录之后，session 就会失效，
但是默认的失效是通过调用 {@link StandardSession#invalidate} 方法来实现的，这一个失效事件无法被 Spring 容器感知到，进而导致当用户注销登录之后，
Spring Security 没有及时清理会话信息表，以为用户还在线，进而导致用户无法重新登录进来。

为了解决这一问题，增加一个 HttpSessionEventPublisher ，这个类实现了 HttpSessionListener 接口，在该 Bean 中，
可以将 session 创建以及销毁的事件及时感知到，并且调用 Spring 中的事件机制将相关的创建和销毁事件发布出去，
进而被 Spring Security 感知到。

#### 实现原理
在用户登录的过程中，会经过 UsernamePasswordAuthenticationFilter，而 UsernamePasswordAuthenticationFilter 中过滤方法的调用是在
AbstractAuthenticationProcessingFilter 中触发的。在 AbstractAuthenticationProcessingFilter#doFilter 方法中，
调用 attemptAuthentication 方法走完认证流程之后，再调用 sessionStrategy.onAuthentication 方法来处理 session 的并发问题；主要
实现代码在 {@link ConcurrentSessionControlAuthenticationStrategy#onAuthentication}，基本流程是：
1. 首先调用 sessionRegistry.getAllSessions 方法获取当前用户的所有 session，该方法在调用时，传递两个参数，一个是当前用户的 authentication，
另一个参数 false 表示不包含已经过期的 session（在用户登录成功后，会将用户的 sessionid 存起来，其中 key 是用户的主体（principal），
value 则是该主题对应的 sessionid 组成的一个集合）。
2. 计算出当前用户 有效 session 个数，同时获取允许的 session 并发数
3. 如果当前 session 数（sessionCount）小于 session 并发数（allowedSessions），则不做任何处理；如果 allowedSessions 的值为 -1，表示对 session 数量不做任何限制。
4. 如果当前 session 数（sessionCount）等于 session 并发数（allowedSessions），那就先看看当前 session 是否不为 null，并且已经存在于 sessions 中了，
如果已经存在了，那都是自家人，不做任何处理；如果当前 session 为 null，那么意味着将有一个新的 session 被创建出来，届时当前 session 数（sessionCount）
就会超过 session 并发数（allowedSessions）。
5. 如果前面的代码中都没能 return 掉，那么将进入策略判断方法 allowableSessionsExceeded 中。
6. allowableSessionsExceeded 方法中，首先会有 exceptionIfMaximumExceeded 属性，这就是我们在 SecurityConfig 中配置的 maxSessionsPreventsLogin 的值，默认为 false；
    1. 如果为 true，就直接抛出异常，那么这次登录就失败了（禁止新登陆）；
    2. 如果为 false，则对 sessions 按照请求时间进行排序，然后再使多余的 session 过期即可（踢掉）。

#### security 是怎么保存 session 对象的？
参见 SessionRegistryImpl 类统一管理：
1. 首先声明了一个支持并发访问的 map 集合 principals 对象，集合的 key 就是用户的主体（principal），正常来说，用户的 principal 其实就是用户对象，
集合的 value 是一个 set 集合，这个 set 集合中保存了这个用户对应的 sessionId。
2. 如有新的 session 需要添加，就在 registerNewSession 方法中进行添加，具体是调用 principals.compute 方法进行添加，key 就是 principal。
3. 如果用户注销登录，sessionId 需要移除，相关操作在 removeSessionInformation 方法中完成，具体也是调用 principals.computeIfPresent 方法。

> ConcurrentMap 集合的 key 是 principal 对象，用对象做 key，一定要重写 equals 方法和 hashCode 方法，否则第一次存完数据，下次就找不到了。
{@link com.syy.security.chapter04.model.domain.UserDO}


### Spring Security 自带防火墙机制
- HttpFirewall 接口：自动处理非法请求
    - 实现类 DefaultHttpFirewall：普通防火墙设置，限制相对于 StrictHttpFirewall 要宽松一些。
    - 实现类 StrictHttpFirewall：SpringSecurity 默认的防火墙设置，严格模式的防火墙设置。
    - DefaultHttpFirewall 与 StrictHttpFirewall 的安全性不可比较，没有谁比谁跟安全。

- 防护措施
    - 只允许 白名单中的方法可以访问，即 不是所有的 HTTP 请求方法都可以执行：{@link org.springframework.security.web.firewall.StrictHttpFirewall.createDefaultAllowedHttpMethods}
    如果需要　发送其他 Http 请求，需要重新提供一个 ScriptHttpFirewall
        ```java
        @Bean
        HttpFirewall httpFirewall() {
            StrictHttpFirewall firewall = new StrictHttpFirewall();
            // 不做 HTTP 请求方法校验，什么方法都可以通过。
            firewall.setUnsafeAllowAnyHttpMethod(true);
            firewall.setAllowedHttpMethods 重新定义 可以通过的方法
            return firewall;
        }
        ```
    - security中 默认 请求地址不能有分号，因为这种方式传参不安全。
        ```java
        // 开放 ; 分号传参
        @Bean
        HttpFirewall httpFirewall() {
            StrictHttpFirewall firewall = new StrictHttpFirewall();
            firewall.setAllowSemicolon(true);
            return firewall;
        }
        ```
    - 必须是标准化的 URL：org.springframework.security.web.firewall.StrictHttpFirewall#isNormalized(javax.servlet.http.HttpServletRequest)
    - 必须是可打印的 ASCII 字符；
    - 双斜杠不被允许；除非配置 StrictHttpFirewall#setAllowUrlEncodedSlash(true)
    - % 百分号不被允许；除非配置 setAllowUrlEncodedPercent(true)
    - \与/ 正、反斜杠 不被允许；除非配置 setAllowBackSlash(true);setAllowUrlEncodedSlash(true)
    - . 点号不被允许；除非配置 setAllowUrlEncodedPeriod(true)

> 以上的 限制 都是针对 request URI 的，而不是 针对 请求参数，请求参数 不被以上限制。
不建议修改 spring security 默认对 URI 的限制，因为都是为了保证不受攻击。







