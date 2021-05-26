[TOC]



### 模块功能点

1. 前后端分离的方式，实现spring boot security
2. 接入数据库和jpa，从数据库中获取用户信息
3. 接入 图形验证码，登陆时需要携带验证码
4. 在 Security 中配置 图形验证码过滤器
5. 图形验证码过滤器中进行验证码校验




### SpringSecurity 的执行流程

```bash
请求——>SecurityContextPersistenceFilter——>UsernamePasswordAuthenticationFilter
——>BasicAuthenticationFilter——>RememberMeAuthenticationFilter
——>……——>ExceptionTranslationFilter——>FilterSecurityInterceptor
```

> 记住密码的时候，将认证信息保存到 session ，并持久化到数据库中



### Security的Remember Me功能

#### successfulAuthentication认证成功后执行的方法

AbstractAuthenticationProcessingFilter#doFilter 是认证类的入口，认证成功后，将执行 AbstractAuthenticationProcessingFilter#successfulAuthentication 方法。



1. SecurityContextHolder.getContext().setAuthentication(authResult)将验证信息存入Spring的上下文中。
2. rememberMeServices.loginSuccess(request, response, authResult)处理记住密码选项。他的具体实现在AbstractRememberMeServices这个抽象类中，当然这个类中的loginSuccess(request, response, authResult)也是什么都没干;具体看 PersistentTokenBasedRememberMeServices#onLoginSuccess(request, response, successfulAuthentication)方法：从验证信息里获取用户名，然后构造token以便持久化到数据库



#### 记住密码后认证，Fileter入口是验证的入口

[Security Remember介绍博客](https://www.jianshu.com/p/d619bb0909b7)
