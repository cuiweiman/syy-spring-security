package com.syy.security.oauth04.controller;

import lombok.extern.slf4j.Slf4j;
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
 * @Description:
 * @Author: cuiweiman
 * @Since: 2021/6/6 下午8:30
 */
@Slf4j
@Controller
public class Oauth04ClientController {

    @Resource
    private RestTemplate restTemplate;

    @GetMapping("/index.html")
    public String hello(String code, Model model) {
        if (code != null) {
            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("code", code);
            map.add("client_id", "CSClient");
            map.add("client_secret", "123");
            map.add("redirect_uri", "http://localhost:9403/index.html");
            map.add("grant_type", "authorization_code");
            Map<String, String> resp = restTemplate.postForObject("http://localhost:9401/oauth/token", map, Map.class);
            String access_token = resp.get("access_token");
            System.out.println("access_token: " + access_token);
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer " + access_token);
            HttpEntity<Object> httpEntity = new HttpEntity<>(headers);
            log.info("access_token：{}", access_token);
            ResponseEntity<String> entity = restTemplate.exchange("http://localhost:9402/admin/hello", HttpMethod.GET, httpEntity, String.class);
            log.info("信息：{}", entity.getBody());
            model.addAttribute("msg", entity.getBody());
        }
        return "index";
    }

}
