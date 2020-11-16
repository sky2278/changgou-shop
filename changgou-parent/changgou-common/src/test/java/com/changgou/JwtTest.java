package com.changgou;

import io.jsonwebtoken.*;
import org.junit.Test;

import java.util.Date;
import java.util.HashMap;

public class JwtTest {

    //  生成token     头非必须    载荷 签证必须 (无签证也能生成一个串但无效)
    @Test
    public void testDemo01() throws Exception {

        //  创建token
        JwtBuilder jwtBuilder = Jwts.builder();

        //  构建数据
        //  头
        HashMap<String, Object> map = new HashMap<>();
        map.put("address", "深圳");
        map.put("school", "北京大学");
        jwtBuilder.setHeader(map);

        //  载荷
        jwtBuilder.setId("1");
        jwtBuilder.setIssuer("jack");
        //jwtBuilder.setIssuedAt(new Date());     //  添加日期每次生成的token会不一样
        //  设置过期时间
        jwtBuilder.setExpiration(new Date(System.currentTimeMillis() + 50000));

        //  自定义载荷
        HashMap<String, Object> claims = new HashMap<>();
        claims.put("name", "jack");
        claims.put("age", 18);
        jwtBuilder.setClaims(claims);

        //  签证
        jwtBuilder.signWith(SignatureAlgorithm.HS256, "cast");

        //  生成token
        String token = jwtBuilder.compact();
        System.out.println(token);

    }

    //  解析token
    @Test
    public void testDemo02() throws Exception {
        //  创建解析对象
        JwtParser parser = Jwts.parser();
        //  设置key\
        parser.setSigningKey("cast");
        String token = "eyJhZGRyZXNzIjoi5rex5ZyzIiwic2Nob29sIjoi5YyX5Lqs5aSn5a2mIiwiYWxnIjoiSFMyNTYifQ.eyJuYW1lIjoiamFjayIsImFnZSI6MTh9.gShZ305M7uRhzQYDSufRim87OFp7N87JDaH1PT5mndw";
        Jws<Claims> claimsJws = parser.parseClaimsJws(token);
        JwsHeader header = claimsJws.getHeader();
        Claims body = claimsJws.getBody();
        String signature = claimsJws.getSignature();
        System.out.println("header:" + header.get("address") + "---" + header.get("school"));
        System.out.println("body:" + body.get("name") + "---" + body.get("age"));
        System.out.println("签名:" + signature);

        System.out.println(claimsJws);

    }

}
