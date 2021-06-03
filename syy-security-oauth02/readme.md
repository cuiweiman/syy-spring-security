## 简化模式

- 第三方应用不需要 先去 获取 code，再获取 token令牌。可以直接 通过链接
http://localhost:9201/oauth/authorize?client_id=CSClient&response_type=token&scope=all&redirect_uri=http://localhost:9203/index.html
发送给 auth-server 认证服务器。
- auth-server 接收到请求后，验证 clientId 等参数是否 符合 第三方平台的配置，如验证通过，则进行 用户登录授权。
- 获取到 用户登录授权 后，auth-server 会重定向到 redirect_uri 页面，并且携带着 token 令牌。
    ```java
    http://localhost:8082/index.html#access_token=9fda1800-3b57-4d32-ad01-05ff700d44cc&token_type=bearer&expires_in=1940
    ```
- 第三方应用 使用 js 截取到 token 令牌参数，并携带到 请求头 请求资源服务器，获取需要的资源。

> 第三方平台 直接 向 auth-server 获取 令牌 token，拿到 token 后直接请求 请求资源服务；
一般如果网站是纯静态页面则可以采用这种方式。



