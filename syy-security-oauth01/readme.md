
## OAuth2
授权服务器：保护资源服务，向第三方进行授权，auth-server
资源服务器：resource-server
第三方应用：client-app，第三方应用 向 授权服务器 获取 资源服务器 的访问 权限。

### OAuth2 的四种授权模式
1. 授权码模式：常见的第三方平台登录功能基本都是使用这种模式。
2. 简化模式：简化模式是不需要客户端服务器参与，直接在浏览器中向授权服务器申请令牌（token），
一般如果网站是纯静态页面则可以采用这种方式。
3. 密码模式：密码模式是用户把用户名密码直接告诉客户端，客户端使用说这些信息向授权服务器申请令牌（token）。
这需要用户对客户端高度信任，例如客户端应用和服务提供商就是同一家公司，自己做前后端分离登录就可以采用这种模式。
4. 客户端模式：客户端模式是指客户端使用自己的名义而不是用户的名义向服务提供者申请授权，严格来说，客户端模式并不能算作
OAuth 协议要解决的问题的一种解决方案，但是，对于开发者而言，在一些前后端分离应用或者为移动端提供的认证授权服务器上
使用这种模式还是非常方便的。

#### 授权码模式
> 分为三个服务：第三方应用、资源服务器、授权服务器。授权服务器 派发 token，使用 token 前往资源服务器获取资源。

1. 用户 A （服务方的用户，例如微信用户）点击 一个 超链接 就会去请求授权服务器（微信的授权服务器），用户点击的过程其实
也就是 第三方应用 跟 用户A 要 授权的过程。
2. 用户点击了超链接之后，向授权服务器发送请求，一般来说 超链接 可能有如下参数：
    ```java
    /*
    * 1、response_type 授权类型，使用授权码模式的时候这里固定为 code，表示要求返回授权码（将来拿着这个授权码去获取 access_token）;
    * 2、client_id 表示客户端 id，即第三方应用的 id。例如 第三方应用想接入 微信登录，需要先 向微信平台 注册第三方应用的信息，
    *   并获取到一个 clientId。
    * 3、redirect_uri 用户登录在成功/失败后，跳转的地址（成功登录微信后，跳转到 www.baidu.com 即 第三方应用 中的哪个页面）
    *   ，跳转的时候，还会携带上一个授权码参数。
    * 4、scope 表示授权范围，即 第三方应用可以使用 这个 token 做什么，一般就是获取用户非敏感的基本信息。
    */
    https://wx.qq.com/oauth/authorize?response_type=code&client_id=baidu&redirect_uri=www.baidu.com&scope=all
    ```
3. 第三方应用平台（这里指 www.baidu.com）使用 client_id 和 client_secret 以及其他一些信息去授权服务器请求 token 令牌，
    微信的授权服务器在校验过这些数据之后，就会发送一个令牌回来。这个过程一般是在后端完成的，而不是利用 js 去完成。
4. 第三方应用拿到 token 后，就可以请求用户信息了。

**通常 授权码模式是四种模式中最安全的一种模式**，因为这种模式的 access_token 不用经过浏览器或者移动端 App，
是直接从我们的后台发送到授权服务器上，这样就很大程度减少了 access_token 泄漏的风险。


#### 简化模式
1. 登录的超链接 https://wx.qq.com/oauth/authorize?response_type=token&client_id=baidu&redirect_uri=www.baidu.com&scope=all
    这里的参数和前面授权码模式的基本相同，只有 response_type 的值不一样，这里是 token，表示要求授权服务器直接返回 access_token。
2. 用户点击这个超链接之后，就会跳转到微信登录页面，然后用户进行登录。
3. 用户登录成功后，微信会自动重定向到 redirect_uri 参数指定的跳转网址，同时携带上 access_token，这样用户在前端就获取到 access_token 了。

> 简化模式的弊端很明显，因为没有后端，所以非常不安全，除非对安全性要求不高，否则不建议使用。纯前端应用，就是只有页面，没有后端，可以使用来接入微信登录。


#### 密码模式
> 密码模式有一个前提就是 高度信任 第三方应用

1. 第三方应用 发送的 登录链接
    ```java
    https://wx.qq.com/oauth/authorize?response_type=password&client_id=baidu&username=baidu&password=123
    ```
2. 微信校验过用户名/密码之后，直接在 HTTP 响应中把 access_token 返回给客户端。


#### 客户端模式
> 有的应用可能没有前端页面，就是一个后台，例如微信公众号的后台开发，可以采用这种认证方式。
客户端模式给出的令牌，就是针对第三方应用的，而不是针对用户的，因此与用户无关。

1.授权链接：

    ```java
    /*
    * grant_type，获取 access_token 填写 client_credential ;
    client_id 和 client_secret 用来确认客户端的身份.
    */
    GET https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&client_id=APPID&client_secret=APPSECRET
    ```

2. 授权服务器通过验证后，会直接返回 access_token 给客户端。


### 本案例请求过程
1. app-client 的前端页面 进行第三方登录,向 auth-server 发起第三方客户端登录请求，clientId、scopeType、redirectUri 信息
都配置在 AuthorizationServer#configure(ClientDetailsServiceConfigurer)
```java
<a href="http://localhost:9101/oauth/authorize?client_id=CSClient&response_type=code&scope=all&redirect_uri=http://localhost:9103/index.html">第三方登录</a>
```
2. auth-server 校验 app-client 登录 url 中的参数，校验通过后，需要用户在 auth-server 服务中 进行个人登录，以及 向 第三方应用
平台进行授权。
3. 登录并授权成功后，redirectUri 的地址中会携带一个 code 参数，即 redirect_uri=http://localhost:9103/index.html?code=***
此时携带着 code，将会访问客户端的这个方法：{@link ClientAppController#hello}。
4. 在 app-client 的 ClientAppController#hello 方法中，携带 code 前往 auth-server 服务中获取 token 令牌。
5. 获取到 令牌 后，又继续 请求 resource-server 资源服务中的 HelloController#admin 方法。
6. 请求成功后，携带着 请求结果，跳转到 index.html 页面。
请求结束。


### PostMan 直接调用
> app-client 服务也可以不需要，直接在 postman 中调用，测试 auth-server 对 resource-server 中服务的 授权和保护。

```json
{
	"info": {
		"_postman_id": "26769c11-3bca-48e3-a38b-c046498a2bfd",
		"name": "syy-spring-security",
		"schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
	},
	"item": [
		{
			"name": "oauth01",
			"item": [
				{
					"name": "第三方平台获取code",
					"request": {
						"method": "GET",
						"header": [],
						"url": {
							"raw": "http://localhost:9101/oauth/authorize?client_id=CSClient&response_type=code&scope=all&redirect_uri=http://localhost:9103/index.html",
							"protocol": "http",
							"host": [
								"localhost"
							],
							"port": "9101",
							"path": [
								"oauth",
								"authorize"
							],
							"query": [
								{
									"key": "client_id",
									"value": "CSClient"
								},
								{
									"key": "response_type",
									"value": "code"
								},
								{
									"key": "scope",
									"value": "all"
								},
								{
									"key": "redirect_uri",
									"value": "http://localhost:9103/index.html"
								}
							]
						}
					},
					"response": []
				},
				{
					"name": "授权服务登录",
					"request": {
						"method": "POST",
						"header": [],
						"url": {
							"raw": "http://localhost:9101/login?username=cwm&password=250",
							"protocol": "http",
							"host": [
								"localhost"
							],
							"port": "9101",
							"path": [
								"login"
							],
							"query": [
								{
									"key": "username",
									"value": "cwm"
								},
								{
									"key": "password",
									"value": "250"
								}
							]
						}
					},
					"response": []
				},
				{
					"name": "同意向第三方平台授权",
					"request": {
						"method": "POST",
						"header": [],
						"url": {
							"raw": "http://localhost:9101/oauth/authorize?scope.all=true&user_oauth_approval=true",
							"protocol": "http",
							"host": [
								"localhost"
							],
							"port": "9101",
							"path": [
								"oauth",
								"authorize"
							],
							"query": [
								{
									"key": "scope.all",
									"value": "true"
								},
								{
									"key": "user_oauth_approval",
									"value": "true"
								}
							]
						}
					},
					"response": []
				}
			]
		}
	]
}
```



## 参考资料
[OAuth2系列](https://mp.weixin.qq.com/mp/appmsgalbum?__biz=MzI1NDY0MTkzNQ==&action=getalbum&album_id=1319833457266163712&scene=173&from_msgid=2247488209&from_itemidx=2&count=3&nolastread=1#wechat_redirect)



