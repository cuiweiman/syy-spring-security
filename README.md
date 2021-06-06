# syy-spring-security
spring security

[技术博客：spring security 系列](https://mp.weixin.qq.com/mp/appmsgalbum?__biz=MzI1NDY0MTkzNQ==&action=getalbum&album_id=1319828555819286528&scene=173&from_msgid=2247488106&from_itemidx=1&count=3&nolastread=1#wechat_redirect)

# spring-security 身份认证流程


# oauth的服务接口
|端点|含义|
|:--|:--|
|/oauth/authorize|这个是授权的端点|
|/oauth/token|这个是用来获取令牌的端点|
|/oauth/confirm_access|用户确认授权提交的端点（就是 auth-server 询问用户是否授权那个页面的提交地址）|
|/oauth/error|授权出错的端点|
|/oauth/check_token|校验 access_token 的端点|
|/oauth/token_key|提供公钥的端点|

