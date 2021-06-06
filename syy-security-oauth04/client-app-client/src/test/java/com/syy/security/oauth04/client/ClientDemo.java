package com.syy.security.oauth04.client;

import com.syy.security.oauth04.SyyOauth04ClientAppTest;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @Description:
 * @Author: cuiweiman
 * @Since: 2021/6/6 下午11:37
 */
public class ClientDemo extends SyyOauth04ClientAppTest {

    @Resource
    RestTemplate restTemplate;

    @Test
    public void contextLoads() {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_id", "CSClient");
        map.add("client_secret", "123");
        map.add("grant_type", "client_credentials");
        Map<String, String> resp = restTemplate.postForObject("http://localhost:9401/oauth/token", map, Map.class);
        String access_token = resp.get("access_token");
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + access_token);
        HttpEntity<Object> httpEntity = new HttpEntity<>(headers);
        ResponseEntity<String> entity = restTemplate.exchange("http://localhost:9402/hello", HttpMethod.GET, httpEntity, String.class);
        System.out.println(entity.getBody());
    }
}
