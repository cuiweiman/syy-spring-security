[TOC]



### 模块功能点

1. 前后端分离的方式，实现spring boot security
2. 接入数据库和jpa，从数据库中获取用户信息
3. 接入 图形验证码，登录时需要携带验证码
4. 在 Security 中配置 图形验证码过滤器
5. 图形验证码过滤器中进行验证码校验
6. 配置 remember me 功能，并提供两种方案，避免 remember me 令牌丢失带来的隐藏风险

> 图形验证码过滤器存在的问题：图形验证 只需要在 登录时进行验证即可，但是这里的自定义 过滤器 ，
虽然根据请求路径进行了筛选，但是路径筛选的主要原因 还是因为 无论任何请求都会经过 这个过滤器。
这个问题在 chapter04 中解决 {com.syy.security.chapter04.config.MyAuthenticationProvider}


### Spring Security 过滤器链

```bash
请求——>SecurityContextPersistenceFilter——>UsernamePasswordAuthenticationFilter
——>BasicAuthenticationFilter——>RememberMeAuthenticationFilter
——>……——>ExceptionTranslationFilter——>FilterSecurityInterceptor
```

> 记住密码的时候，将认证信息保存到 session ，并持久化到数据库中

### 当用户发送登录请求时
登录请求——>UsernamePasswordAuthenticationFilter——>(未认证)AuthenticationManager接口——>AuthenticationProvider接口
——>UserDetailsService接口加载用户信息数据——>UserDetails接口提供核心用户信息——>Authentication(已认证)


### Security的Remember Me功能

#### successfulAuthentication认证成功后执行的方法

AbstractAuthenticationProcessingFilter#doFilter 是认证类的入口，认证成功后，将执行 AbstractAuthenticationProcessingFilter#successfulAuthentication 方法。

1. SecurityContextHolder.getContext().setAuthentication(authResult)将验证信息存入Spring的上下文中。
2. rememberMeServices.loginSuccess(request, response, authResult)处理记住密码选项。他的具体实现在AbstractRememberMeServices这个抽象类中，当然这个类中的loginSuccess(request, response, authResult)也是什么都没干;具体看 PersistentTokenBasedRememberMeServices#onLoginSuccess(request, response, successfulAuthentication)方法：从验证信息里获取用户名，然后构造token以便持久化到数据库


#### 记住密码的安全问题
> 如果令牌丢失或者被他人盗取，那就可以随便使用这个令牌访问系统，会造成一系列安全问题。需要采用技术手段解决。

1. 修改令牌持久化方案：在持久化令牌中，新增了两个经过 MD5 散列函数计算的校验参数，一个是 series，另一个是 token。其中，series 只有当用户在使用用户名/密码登录时，才会生成或者更新，而 token 只要有新的会话，就会重新生成，这样就可以避免一个用户同时在多端登录，
就像手机 QQ ，一个手机上登录了，就会踢掉另外一个手机的登录，这样用户就会很容易发现账户是否泄漏。
    1. {@link PersistentRememberMeToken} 令牌保存处理类。
    1. 首先需要一张表来记录令牌信息，这张表可以完全自定义，也可以使用系统默认提供的 JDBC 来操作，如果使用默认的 JDBC，即 {@link JdbcTokenRepositoryImpl}。
    ```sql
    CREATE TABLE `persistent_logins` (
      `username` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
      `series` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
      `token` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
      `last_used` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
      PRIMARY KEY (`series`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
    ```

2. 二次校验：如果用户使用了自动登录功能，我们可以只让他做一些常规的不敏感操作，例如数据浏览、查看，但是不允许他做任何修改、删除操作，如果用户点击了修改、删除按钮，
我们可以跳转回登录页面，让用户重新输入密码确认身份，然后再允许他执行敏感操作。



#### 记住密码后认证，Fileter入口是验证的入口

[Security Remember介绍博客](https://www.jianshu.com/p/d619bb0909b7)


