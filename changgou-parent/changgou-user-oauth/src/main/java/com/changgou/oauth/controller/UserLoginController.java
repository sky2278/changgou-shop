package com.changgou.oauth.controller;

import com.changgou.oauth.service.UserLoginService;
import com.changgou.oauth.util.AuthToken;
import entity.Result;
import entity.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/user")
public class UserLoginController {

    @Autowired
    private UserLoginService userLoginService;

    //  客户端名称
    @Value("${auth.clientId}")
    private String clientId;

    //  客户端公钥
    @Value("${auth.clientSecret}")
    private String clientSecret;

    /**
     *  用户登入
     * @param username
     * @param password
     * @return
     */
    @RequestMapping("/login")
    public Result login(String username, String password, HttpServletResponse response) {
        try {
            //  指定授权模式  password
            String grant_type = "password";
            //  生成令牌
            AuthToken authToken = userLoginService.login(username, password, grant_type, clientId, clientSecret);
            //  保存token (cookie)    先生成token 网关将token添加到请求头 则之后请求头中也有token
            Cookie cookie = new Cookie("Authorization",authToken.getAccessToken());
            cookie.setPath("/");
            cookie.setDomain("localhost");
            response.addCookie(cookie);
            return new Result(true, StatusCode.OK, "登入成功!", authToken);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, StatusCode.LOGINERROR, "登入失败!");
        }
    }

}
