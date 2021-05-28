
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







