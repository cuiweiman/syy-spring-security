[TOC]

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


### 会话固定攻击
#### HttpSession
HttpSession 是一个服务端的概念，服务端生成的 HttpSession 都会有一个对应的 sessionId，这个 sessionId 会通过 cookie 传递给前端，
前端以后发送请求的时候，就带上这个 sessionId 参数，服务端看到这个 sessionId 就会把这个前端请求和服务端的某一个 HttpSession 对应起来
形成“会话”的感觉。

浏览器关闭并不会导致服务端的 HttpSession 失效，想让服务端的 HttpSession 失效，要么手动调用 HttpSession#invalidate 方法；
要么等到 session 自动过期；要么重启服务端。

但是为什么有的人会感觉浏览器关闭之后 session 就失效了呢？这是因为浏览器关闭之后，保存在浏览器里边的 sessionId 就丢了（默认情况下），
所以当浏览器再次访问服务端的时候，服务端会给浏览器重新分配一个 sessionId ，这个 sessionId 和之前的 HttpSession 对应不上，
所以用户就会感觉 session 失效。可以通过手动配置，让浏览器重启之后 sessionId 不丢失，但是这样会带来安全隐患，所以一般不建议。

在服务端的响应头中有一个 Set-Cookie 字段，该字段指示浏览器更新 sessionId，同时还有一个 **HttpOnly** 属性，这个表示通过
JS 脚本无法读取到 Cookie 信息，这样能有效的防止 XSS 攻击。

#### 会话固定攻击
session fixation attack。正常来说，只要你不关闭浏览器，并且服务端的 HttpSession 也没有过期，那么维系服务端和浏览器的 sessionId
是不会发生变化的，而会话固定攻击，则是利用这一机制，借助受害者用相同的会话 ID 获取认证和授权，然后利用该会话 ID 劫持受害者的会话以
成功冒充受害者，造成会话固定攻击。

**会话固定攻击 流程：**
1. 攻击者自己可以正常访问淘宝网站，在访问的过程中，淘宝网站给攻击者分配了一个 sessionId。
2. 攻击者利用自己拿到的 sessionId 构造一个淘宝网站的链接，并把该链接发送给受害者。
3. 受害者使用该链接登录淘宝网站（该链接中含有 sessionId），登录成功后，一个合法的会话就成功建立。
4. 攻击者利用手里的 sessionId 冒充受害者。

#### 如何防御？
> 这个问题的根源在 sessionId 不变，如果用户在未登录时拿到的是一个 sessionId，登录之后服务端给用户重新换一个 sessionId，就可以防止会话固定攻击了。

**SpringSecurity默认支持 预防 会话固定攻击,主要体现在：**
1. 请求地址中有 分号 ; 请求会被直接拒绝。
2. 响应的 Set-Cookie 字段中有 HttpOnly 属性，这种方式避免了通过 XSS 攻击来获取 Cookie 中的会话信息进而达成会话固定攻击。
3. 让 sessionId 变一下。既然问题是由于 sessionId 不变导致的，那我就让 sessionId 变一下：.sessionManagement().sessionFixation().**();
    1. migrateSession：在登录成功之后，创建一个新的会话，然后讲旧的 session 中的信息复制到新的 session 中，「默认即此」。
    2. none：表示不做任何事情，继续使用旧的 session。
    3. changeSessionId：表示 session 不变，但是会修改 sessionId，这实际上用到了 Servlet 容器提供的防御会话固定攻击。
    4. newSession：表示登录后创建一个新的 session。

```java
http.csrf().csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
.and()
.sessionManagement()
// 防止 会话固定攻击，默认 即是 如此
.sessionFixation().migrateSession()
```




### 使用 Redis 实现 共享 Session 解决方案
引入依赖，增添 redis 服务器连接 配置，然后在 SecurityConfig 中配置如下。security 会自动将 session 注册到 redis 中。

> 注意需 去除 配置的 HttpSessionEventPublisher 创建销毁 session 事件的感知发布事件，否则仍然会使用 security 内部的 session 注册表。

```java
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Resource
    FindByIndexNameSessionRepository sessionRepository;
    @Bean
    SpringSessionBackedSessionRegistry sessionRegistry() {
        return new SpringSessionBackedSessionRegistry(sessionRepository);
    }
}
```


### CSRF攻击：跨域请求伪造
#### CSRF 攻击方式
[security 中防御 CSRF 攻击](https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247488656&idx=2&sn=f00c9c9d51caf76caa76a813961ba38a)
1. 假设用户打开了招商银行网上银行网站，并且登录。
2. 登录成功后，网上银行会返回 Cookie 给前端，浏览器将 Cookie 保存下来。
3. 用户在没有登出网上银行的情况下，在浏览器里边打开了一个新的选项卡，然后又去访问了一个危险网站。
4. 这个危险网站上有一个超链接，超链接的地址指向了招商银行网上银行。
5. 用户点击了这个超链接，由于这个超链接会自动携带上浏览器中保存的 Cookie，所以用户不知不觉中就访问了网上银行，进而可能给自己造成了损失。

> 在 spring security 中，防御csrf攻击 的功能是 默认开启的，测试 csrf 时可以关闭：http.and().csrf().disable();

#### CSRF 防御思路
CSRF 防御，一个核心思路就是在前端请求中，添加一个随机数。
因为在 CSRF 攻击中，黑客网站其实是不知道用户的 Cookie 具体是什么的，他是让用户自己发送请求到网上银行这个网站的，因为这个过程会自动携带上 Cookie 中的信息。
所以防御思路就是这样：用户在访问网上银行时，除了携带 Cookie 中的信息之外，还需要携带一个随机数，如果用户没有携带这个随机数，则网上银行网站会拒绝该请求。
黑客网站诱导用户点击超链接时，会自动携带上 Cookie 中的信息，但是却不会自动携带随机数，这样就成功的避免掉 CSRF 攻击了。

> Spring Security 中默认就提供了 csrf 防御，但是需要开发者做的事情比较多。
- 前后端不分离，
- 前后端分离,配置如下。将 _csrf 参数放在 Cookie 中返回前端。
    1. 黑客网站根本不知道 Cookie 里边存的啥，也不需要知道，因为 CSRF 攻击是浏览器自动携带上 Cookie 中的数据的。
    2. 将服务端生成的随机数放在 Cookie 中，前端需要从 Cookie 中提取出来 _csrf 参数，然后拼接成参数传递给后端，单纯的将 Cookie 中的数据传到服务端是没用的。
    ```java
    /*
    * 配置的时候通过 withHttpOnlyFalse 方法获取了 CookieCsrfTokenRepository 的实例，该方法会设置 Cookie 中的 HttpOnly 属性为 false，
    * 也就是允许前端通过 js 操作 Cookie（否则前端没有办法获取到 _csrf）。
    */
    http.csrf().csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
    ```

#### security 中 CSRF 源码分析

> 大致过程：
> - 生成 csrfToken 保存在 HttpSession 或者 Cookie 中。
> - 请求到来时，从请求中提取出来 csrfToken，和保存的 csrfToken 做比较，进而判断出当前请求是否合法。

- csrf 参数对象：CsrfToken，有两个实现类，DefaultCsrfToken（默认）、SaveOnAccessCsrfToken。
- CsrfToken 的生成与保存：CsrfTokenRepository 接口，有四个实现类。
    - generateToken 方法就是 CsrfToken 的生成过程。
    - saveToken 方法就是保存 CsrfToken。
    - loadToken 则是如何加载 CsrfToken。
- 参数校验：CsrfFilter#doFilterInternal

### 两种账号密码的加密方式
#### commons-codec
```xml
<dependency>
    <groupId>commons-codec</groupId>
    <artifactId>commons-codec</artifactId>
</dependency>
```
```java
@Component
public class MyPasswordEncoder implements PasswordEncoder {
    // 对密码进行加密，参数 rawPassword 就是你传入的明文密码，返回的则是加密之后的密文，这里的加密方案采用了 MD5。
    @Override
    public String encode(CharSequence rawPassword) {
        return DigestUtils.md5DigestAsHex(rawPassword.toString().getBytes());
    }

    // 对密码进行比对，参数 rawPassword 相当于是用户登录时传入的密码，encodedPassword 则相当于是加密后的密码（从数据库中查询而来）
    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return encodedPassword.equals(DigestUtils.md5DigestAsHex(rawPassword.toString().getBytes()));
    }
}
```
> 用户在登录时，就会自动调用 matches 方法进行密码比对。
  当然，在用户注册时，需要 将 密码加密后 再存入数据库中。

##### SpringSecurity 提供的 BCryptPasswordEncoder
```java
// Security 中配置的 密码加密 方法
@Bean
PasswordEncoder passwordEncoder() {
    // 即密钥的迭代次数（也可以不配置，默认为 10）
    return new BCryptPasswordEncoder(10);
}
```
```java
// 用户注册时，需要 使用 BCryptPasswordEncoder 对 密码 进行加密。
public int reg(String username, String password) {
    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);
    String encodePasswod = encoder.encode(password);
    return saveToDb(username, encodePasswod);
}
```

### 资源放行的两种策略
#### 静态资源：SecurityConfig#configure
如果是静态资源，直接配置在 方法中，不会经过 security 的过滤器链，直接不被 security 保护。
```java
@Override
public void configure(WebSecurity web) throws Exception {
    web.ignoring().antMatchers("/js/**", "/css/**", "/images/**", "/verifyCode/**");
}
```

#### 后台接口的暴露方式：
后台接口 也可以像 静态资源 一样配置，但是最好经过 security 过滤器链 的校验，因为这样在用户登录后才能拿到用户的登录信息。

如果是后台接口的暴露，需要根据情况，采用不同的暴露策略：ExpressionUrlAuthorizationConfigurer
- hasAnyRole：满足任何一个角色即可
- hasRole：符合这一个角色
- hasAuthority：拥有这一个 权限
- hasAnyAuthority：满足任何一个权限即可
- hasIpAddress：指定 IP 地址可以访问
- permitAll：允许所有用户访问
- anonymous：指定匿名允许访问
- rememberMe：指定 路径 允许 rememberMe 用户可以访问
- denyAll：指定路径不被任意用户访问
- authenticated：指定路径需要被验证后才能访问
- fullyAuthenticated：指定路径 必须 通过 用户名/密码 验证，rememberMe 也不行。
- access：允许指定URL由任意表达式保护

```java
http.authorizeRequests()
    // 指定 任何人 都允许使用URL，不需要登陆。
    .antMatchers("/vc.jpg").permitAll()
    // 指定 其它URL，只允许被经过身份验证的用户访问
    .anyRequest().authenticated();
```

> 请求到达 UsernamePasswordAuthenticationFilter 之前，都会经过 SecurityContextPersistenceFilter 过滤器。
> 1. SecurityContextPersistenceFilter 继承自 GenericFilterBean，而 GenericFilterBean 则是 Filter 的实现，
所以 SecurityContextPersistenceFilter 最重要的方法就是 doFilter 了。
> 2. 在 doFilter 方法中，它首先会从 repo 中读取一个 SecurityContext 出来，这里的 repo 实际上就是 HttpSessionSecurityContextRepository，
读取 SecurityContext 的操作会进入到 readSecurityContextFromSession 方法中，在这里我们看到了读取的核心方法
Object contextFromSession = httpSession.getAttribute(springSecurityContextKey);，这里的 springSecurityContextKey 对象的值
就是 SPRING_SECURITY_CONTEXT，读取出来的对象最终会被转为一个 SecurityContext 对象。
> 3. SecurityContext 是一个接口，它有一个唯一的实现类 SecurityContextImpl，这个实现类其实就是用户信息在 session 中保存的 value。
> 4. 在拿到 SecurityContext 之后，通过 SecurityContextHolder.setContext 方法将这个 SecurityContext 设置到 ThreadLocal 中去，这样，
在当前请求中，Spring Security 的后续操作，我们都可以直接从 SecurityContextHolder 中获取到用户信息了。
> 5. 接下来，通过 chain.doFilter 让请求继续向下走（这个时候就会进入到 UsernamePasswordAuthenticationFilter 过滤器中了）。
> 6. 在过滤器链走完之后，数据响应给前端之后，finally 中还有一步收尾操作，这一步很关键。这里从 SecurityContextHolder 中获取到 SecurityContext，
获取到之后，会把 SecurityContextHolder 清空，然后调用 repo.saveContext 方法将获取到的 SecurityContext 存入 session 中。

每一个请求到达服务端的时候，首先从 session 中找出来 SecurityContext ，然后设置到 SecurityContextHolder 中去，方便后续使用，
当这个请求离开的时候，SecurityContextHolder 会被清空，SecurityContext 会被放回 session 中，方便下一个请求来的时候获取;
登录请求来的时候，还没有登录用户数据，但是登录请求走的时候，会将用户登录数据存入 session 中，下个请求到来的时候，就可以直接取出来用了。

1. 如果暴露登录接口的时候，使用了 静态资源的方式，没有走 Spring Security，过滤器链，则在登录成功后，就不会将登录用户信息存入 session 中，
进而导致后来的请求都无法获取到登录用户信息（后来的请求在系统眼里也都是未认证的请求）；
2. 如果登录请求正常，走了 Spring Security 过滤器链，但是后来的 A 请求没走过滤器链（采用前面提到的第一种方式放行），
那么 A 请求中，无法通过 SecurityContextHolder 获取到登录用户信息的，因为它一开始就没经过 SecurityContextPersistenceFilter 过滤器链。




