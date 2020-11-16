package com.changgou.oauth.config;

import com.changgou.oauth.util.JwtToken;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

//  生成带有admin权限的token
@Component
public class FeignOauth2RequestInterceptor implements RequestInterceptor {


    @Override
    public void apply(RequestTemplate template) {

        //  获取请求中所有的头信息 ctrl n
        //  hystrix配置成信号量隔离
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            //  获取所有头信息的名称
            Enumeration<String> headerNames = request.getHeaderNames();
            if (headerNames != null) {
                while (headerNames.hasMoreElements()) {
                    //  头信息名
                    String name = headerNames.nextElement();
                    //  头信息内容
                    String value = request.getHeader(name);
                    //  将所有的头信息保存到头信息中 (保存到feign中)
                    template.header(name, value);
                }
            }
        }

        //  生成token
        String token = JwtToken.adminJwt();
        //  保存到请求头中
        //System.out.println(token);
        template.header("Authorization", "bearer " + token);
    }
}
