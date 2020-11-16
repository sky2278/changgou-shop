package com.changgou.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;

@Component
public class AuthorizeFilter implements GlobalFilter, Ordered {

    // 定义常量                                         Authorization
    private static final String AUTHORIZE_TOKEN = "Authorization";
    private static final String LOGIN_URL = "http://localhost:9001/oauth/login";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        //  判断用户是否登入    如果是登入就直接放行
        //  用户请求的路径
        //  http://cloud.itheima.com:8001/api/user/login?username=szitheima&password=szitheima
        URI uri = request.getURI();
        //  /api/user/login
        String path = uri.getPath();    //  path    /api/brand/1115
        if (path.startsWith("/api/user/login")) {
            //  登入可直接放行
            return chain.filter(exchange);
        }

        //获取token 获取请求参数体 判断cookie中 请求头中是否包含token
        //  请求参数中获取
        MultiValueMap<String, String> queryParams = request.getQueryParams();
        String token = queryParams.getFirst(AUTHORIZE_TOKEN);
        if (StringUtils.isEmpty(token)) {
            //  请求头中获取
            token = request.getHeaders().getFirst(AUTHORIZE_TOKEN);

            if (StringUtils.isEmpty(token)) {
                //cookie中获取
                HttpCookie cookie = request.getCookies().getFirst(AUTHORIZE_TOKEN);
                if (cookie != null) {
                    token = cookie.getValue();
                }
            }
        }
        //  token为空
        if (StringUtils.isEmpty(token)) {
            //  token为空 设置错误响应状态码
            //response.setStatusCode(HttpStatus.UNAUTHORIZED);
            //return response.setComplete();

            //  如果用户访问受保护的资源将用户跳转到登入页面
            System.out.println("此操作需登入!!!");
            response.setStatusCode(HttpStatus.SEE_OTHER);
            //  跳转到登入页面
            String url = request.getURI().toString();
            String pathS = LOGIN_URL + "?ReturnUrl=" + url;
            response.getHeaders().add("Location", pathS);
            return response.setComplete();

        }
        try {
            //  解析成功 放行
            //Claims claims = JwtUtil.parseJWT(token);
            //return chain.filter(exchange);

            //  将token放到请求头中   交给对应的服务去解析
            request.mutate().header("Authorization", "Bearer " + token);
        } catch (Exception e) {
            e.printStackTrace();
            //  解析失败 设置错误响应状态码
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }
        //  放行
        return chain.filter(exchange);
    }

    /**
     * 指定过滤器的加载顺序
     *
     * @return
     */
    @Override
    public int getOrder() {
        return 0;
    }
}
