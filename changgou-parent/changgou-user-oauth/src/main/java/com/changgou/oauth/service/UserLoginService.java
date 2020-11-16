package com.changgou.oauth.service;

import com.changgou.oauth.util.AuthToken;

public interface UserLoginService {

    /**
     *  用户登入
     * @param username  用户名
     * @param password  登入密码
     * @param grant_type    授权方式
     * @param clientId  客户端名称
     * @param clientSecret  客户端公钥
     * @return
     */
    AuthToken login(String username, String password, String grant_type, String clientId, String clientSecret);
}
