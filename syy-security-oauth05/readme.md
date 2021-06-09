
## OAuth2 与 JWT
### 无状态登录与有状态登录
- **有状态登录：** 即服务端需要记录每次会话的客户端信息，从而识别客户端身份，根据用户身份进行请求的处理，典型的设计如 Tomcat
中的 Session。例如登录：用户登录后，我们把用户的信息保存在服务端 session 中，并且给用户一个 cookie 值，记录对应的 session，
然后下次请求，用户携带 cookie 值来（这一步有浏览器自动完成），我们就能识别到对应 session，从而找到用户的信息。
这种方式目前来看最方便，但是也有一些缺陷，如下：
    - 服务端保存大量数据，增加服务端压力;
    - 服务端保存用户状态，不支持集群化部署;
- **无状态登录：** 服务端不保存任何客户端请求者信息, 客户端的每次请求必须具备自描述信息，通过这些信息识别客户端身份。优点：
    - 客户端请求不依赖服务端的信息，多次请求不需要必须访问到同一台服务器（session不共享时，需要指定到同一台服务器）；
    - 服务端的集群和状态对客户端透明；
    - 服务端可以任意的迁移和伸缩（可以方便的进行集群化部署，不受 session 存储的限制）；
    - 减小服务端存储压力（不必存储 session）。

### JWT
Json Web Token，是一种 JSON 风格的轻量级的授权和身份认证规范，可实现无状态、分布式的 Web 应用授权，[常用的java实现](https://github.com/jwtk/jjwt)

#### JWT 数据格式
- Header 头部：声明 类型是 JWT，加密算法可以自定义；对头部进行 Base64Url 编码（可解码），得到第一部分数据。
- Playload 载荷：有效数据，在官方文档中(RFC7519)，这里给了 7 个示例信息。这部分也会采用 Base64Url 编码，得到第二部分数据。
    - iss (issuer)：表示签发人
    - exp (expiration time)：表示token过期时间
    - sub (subject)：主题
    - aud (audience)：受众
    - nbf (Not Before)：生效时间
    - iat (Issued At)：签发时间
    - jti (JWT ID)：编号
- Signature：签名，是整个数据的认证信息。一般根据前两步的数据，再加上服务的的密钥 secret（密钥保存在服务端，不能泄露给客户端），
通过 Header 中配置的加密算法生成。用于验证整个数据完整和可靠性。

> eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.
eyJhdWQiOlsiaWlwLXF1YW56aG91Il0sInVzZXJfbmFtZSI6IntcImlkXCI6MixcInVzZXJuYW1lXCI6XCJzeXN0ZW1BZG1pblwiLFwicGFzc3dvcmRcIjpcImpaYWU3MjdLMDhLYU9tS1NnT2FHend3L1hWcUdyL1BLRWdJTWtqcmNiSkk9XCIsXCJmdWxsbmFtZVwiOlwi5Yu_5Yig57O757uf55So5oi3XCIsXCJtb2JpbGVcIjpcIjExMFwiLFwiY3JlYXRlVGltZVwiOlwiMjAyMC0wMy0xOCAxMjoyOTo1OFwifSIsInNjb3BlIjpbIkFETUlOIiwiVVNFUiIsIkFQSSJdLCJleHAiOjIyMjQ1ODg4MDksImF1dGhvcml0aWVzIjpbInN5c3RlbUFkbWluIl0sImp0aSI6ImQxMzI3Y2NlLWExOGQtNDNhNS04ZGNlLWI4NTk5NGQ0OGFlZCIsImNsaWVudF9pZCI6ImlpcC1xdWFuemhvdSJ9.
lndqzUt_5nC6eCgoxFSv6_FwcZlVxoaHRe0hbBtI178

#### JWT 交互方式
1. 应用程序或客户端向授权服务器请求授权
2. 获取到授权后，授权服务器会向应用程序返回访问令牌
3. 应用程序使用访问令牌来访问受保护资源（如API）

因为 JWT 签发的 token 中已经包含了用户的身份信息，并且每次请求都会携带，这样服务的就无需保存用户信息，
甚至无需去数据库查询，这样就符合了 RESTful 的无状态规范。

#### JWT 存在的问题
1. 续签问题，这是被很多人诟病的问题之一，传统的 cookie+session 的方案天然的支持续签，但是 jwt 由于服务端不保存用户状态，
因此很难完美解决续签问题，如果引入 redis，虽然可以解决问题，但是 jwt 也变得不伦不类了。
2. 注销问题，由于服务端不再保存用户信息，所以一般可以通过修改 secret 来实现注销，服务端 secret 修改后，已经颁发的未过期的
token 就会认证失败，进而实现注销，不过毕竟没有传统的注销方便。
3. 密码重置，密码重置后，原本的 token 依然可以访问系统，这时候也需要强制修改 secret。
4. 基于第 2 点和第 3 点，一般建议不同用户取不同 secret。

#### OAuth2 存在的问题
授权服务器 向 客户端 派发 access-token 后，客户端使用access-token前往访问资源服务器，资源服务器还需要使用 token 前往 授权服务器
进行 access-token 有效性的校验，在高并发环境下这样的校验方式显然是有问题的。
```java
@Bean
RemoteTokenServices tokenServices() {
    RemoteTokenServices services = new RemoteTokenServices();
    services.setCheckTokenEndpointUrl("http://localhost:9101/oauth/check_token");
    services.setClientId("CSClient");
    services.setClientSecret("123");
    return services;
}
```
> **如果 OAuth2 结合 JWT，用户的所有信息都保存在 JWT 中，这样就可以有效的解决上面的问题**

## Token 与 JWT 转换的原理
access-token 生成在 {@link DefaultTokenServices#createAccessToken}，从中可以得知：
1. 默认生成的 access_token 其实就是一个 UUID 字符串。
2. getAccessTokenValiditySeconds 方法用来获取 access_token 的有效期，这个数字是从数据库中查询出来的，
其实就是配置的 access_token 的有效期，配置的有效期单位是秒。
3. 如果设置的 access_token 有效期大于 0，则调用 setExpiration 方法设置过期时间，过期时间就是在当前时间基础上
加上用户设置的过期时间，注意乘以 1000 将时间单位转为毫秒。
4. 接下来设置刷新 token 和授权范围 scope（刷新 token 的生成过程在 createRefreshToken 方法中，和 access_token 的生成过程类似）。
5. 最后面 return 比较关键，这里会判断有没有 accessTokenEnhancer，如果 accessTokenEnhancer 不为 null，则在 accessTokenEnhancer
 中再处理一遍才返回，accessTokenEnhancer 中再处理一遍就比较关键了，就是 access_token 转为 jwt 字符串的过程

这里的 accessTokenEnhancer 实际上是一个 TokenEnhancerChain，这个链中有一个 delegates 变量保存了我们定义的两个 TokenEnhancer
（auth-server 中定义的 JwtAccessTokenConverter 和 CustomAdditionalInformation），也就是说，我们的 access_token 信息将在这两个
类中进行二次处理。「处理的顺序是按照集合中保存的顺序，就是先在 JwtAccessTokenConverter 中处理，后在 CustomAdditionalInformation
中处理，顺序不能乱，也意味着我们在 auth-server 中定义的时候，JwtAccessTokenConverter 和 CustomAdditionalInformation
 的顺序不能写错。」

##  JwtAccessTokenConverter#enhance
> 无论是 JwtAccessTokenConverter 还是 CustomAdditionalInformation，它里边核心的方法都是 enhance

1. 首先构造一个 DefaultOAuth2AccessToken 对象。
2. 将 accessToken 中的附加信息拿出来（此时默认没有附加信息）。
3. 获取旧的 access_token（就是上一步 UUID 字符串），将之作为附加信息存入到 info 中（第四小节测试中，返回的 jwt 中有一个 jti，
其实就是这里存入进来的）。
4. 将附加信息存入 result 中。
5. 对 result 进行编码，将编码结果作为新的 access_token，这个编码的过程就是 jwt 字符串生成的过程。
6. 接下来是处理刷新 token，刷新 token 如果是 jwt 字符串，则需要有一个解码操作，否则不需要，刷新 token 如果是
ExpiringOAuth2RefreshToken 的实例，表示刷新 token 已经过期，则重新生成一个。

## JwtAccessTokenConverter#encode : jwt 的编码过程
首先是把用户信息和 access_token 生成一个 JSON 字符串，然后调用 JwtHelper.encode 方法进行 jwt 编码。


