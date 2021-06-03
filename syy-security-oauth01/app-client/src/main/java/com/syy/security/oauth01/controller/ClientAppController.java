package com.syy.security.oauth01.controller;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @Description: 第三方客户端平台
 * @Author: cuiweiman
 * @Since: 2021/6/3 上午11:52
 */
@Controller
public class ClientAppController {

    @Resource
    private RestTemplate restTemplate;

    /**
     * 根据拿到的 code，去请求 http://localhost:9101/oauth/token 地址去获取 Token，返回的数据结构如下：
     * access_token 就是我们请求数据所需要的令牌，refresh_token 则是我们刷新 token 所需要的令牌，expires_in 表示 token 有效期还剩多久。
     * <pre class="code">
     * {
     *     "access_token": "e7f223c4-7543-43c0-b5a6-5011743b5af4",
     *     "token_type": "bearer",
     *     "refresh_token": "aafc167b-a112-456e-bbd8-58cb56d915dd",
     *     "expires_in": 7199,
     *     "scope": "all"
     * }
     * </pre>
     * <p>
     * 接下来，根据 access_token，去请求资源服务器，注意 access_token 通过请求头传递，最后将资源服务器返回的数据放到 model 中。
     */
    @GetMapping("/index.html")
    public String hello(String code, Model model) {
        if (code != null) {
            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("code", code);
            map.add("client_id", "CSClient");
            map.add("client_secret", "123");
            map.add("redirect_uri", "http://localhost:9103/index.html");
            map.add("grant_type", "authorization_code");
            Map<String, String> resp = restTemplate.postForObject("http://localhost:9101/oauth/token", map, Map.class);
            String access_token = resp.get("access_token");
            System.out.println(access_token);
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer " + access_token);
            HttpEntity<Object> httpEntity = new HttpEntity<>(headers);
            ResponseEntity<String> entity = restTemplate.exchange("http://localhost:9102/admin/hello", HttpMethod.GET, httpEntity, String.class);
            model.addAttribute("msg", entity.getBody());
        }
        return "index";
    }

}
