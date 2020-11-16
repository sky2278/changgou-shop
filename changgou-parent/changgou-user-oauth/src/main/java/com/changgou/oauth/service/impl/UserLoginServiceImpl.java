package com.changgou.oauth.service.impl;

import com.changgou.oauth.service.UserLoginService;
import com.changgou.oauth.util.AuthToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Map;

@Service
public class UserLoginServiceImpl implements UserLoginService {

    @Autowired
    private LoadBalancerClient loadBalancerClient;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * 用户登入
     *
     * @param username     用户名
     * @param password     登入密码
     * @param grant_type   授权方式
     * @param clientId     客户端名称
     * @param clientSecret 客户端公钥
     * @return
     */
    @Override
    public AuthToken login(String username, String password, String grant_type, String clientId, String clientSecret) {

        //  获取服务实例
        ServiceInstance serviceInstance = loadBalancerClient.choose("user-auth");
        //  获取服务路径
        String uri = serviceInstance.getUri().toString();

        //  设置生成token的url
        String url = uri + "/oauth/token";

        //  封装请求参数 发送请求
        //  args1:  生成token路径
        //  args2:  请求方式(必须为POST)
        //  args3:  请求参数体
        //  args4:  返回值类型

        //  请求体
        LinkedMultiValueMap body = new LinkedMultiValueMap();
        body.add("grant_type",grant_type);
        body.add("username",username);
        body.add("password",password);
        //  请求头
        LinkedMultiValueMap headers = new LinkedMultiValueMap();
        //  对客户端名称和客户单公钥进行base64编码
        String encodeToString = Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes());
        headers.add("Authorization","Basic "+encodeToString);
        //  封装
        HttpEntity requestEntity = new HttpEntity(body,headers);
        ResponseEntity<Map> responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);

        Map<String,Object> map = responseEntity.getBody();
        AuthToken authToken = new AuthToken();
        authToken.setAccessToken(map.get("access_token").toString());
        authToken.setRefreshToken(map.get("refresh_token").toString());
        authToken.setJti(map.get("jti").toString());
        return authToken;
    }
}
