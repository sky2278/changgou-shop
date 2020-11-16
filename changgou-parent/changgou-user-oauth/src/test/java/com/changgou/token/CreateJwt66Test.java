package com.changgou.token;

import com.alibaba.fastjson.JSON;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaSigner;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName CreateJwt66Test
 * @Description
 * @Author 传智播客
 * @Date 18:40 2019/8/21
 * @Version 2.1
 **/
public class CreateJwt66Test {

    /***
     * 创建令牌测试
     */
    @Test
    public void testCreateToken(){
        //证书文件路径
        String key_location="changgou89.jks";
        //秘钥库密码
        String key_password="changgou89";
        //秘钥密码
        String keypwd = "changgou89";
        //秘钥别名
        String alias = "changgou89";

        //访问证书路径
        ClassPathResource resource = new ClassPathResource(key_location);

        //创建秘钥工厂
        KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(resource,key_password.toCharArray());

        //读取秘钥对(公钥、私钥)
        KeyPair keyPair = keyStoreKeyFactory.getKeyPair(alias,keypwd.toCharArray());

        //获取私钥
        RSAPrivateKey rsaPrivate = (RSAPrivateKey) keyPair.getPrivate();

        //定义Payload
        Map<String, Object> tokenMap = new HashMap<>();
        tokenMap.put("id", "1");
        tokenMap.put("name", "itheima");
        tokenMap.put("roles", "ROLE_VIP,ROLE_USER");

        //生成Jwt令牌
        Jwt jwt = JwtHelper.encode(JSON.toJSONString(tokenMap), new RsaSigner(rsaPrivate));

        //取出令牌
        String encoded = jwt.getEncoded();
        System.out.println(encoded);
    }


    /***
     * 校验令牌
     */
    @Test
    public void testParseToken(){
        //令牌
        String token = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJyb2xlcyI6IlJPTEVfVklQLFJPTEVfVVNFUiIsIm5hbWUiOiJpdGhlaW1hIiwiaWQiOiIxIn0.jxcmIlSGr6kxa4an1eeK9j3rYbzH-KeJu_g7yRyVE1e2OOx-mhtO0sneXrDJEeO2lvX92Si3AgyGcvPowwtqRcw7PYBEHeXZWQjZUpOJqJ1myNtveyT0idG3WjB2S0bjUILTy48XkleuoDwAjnLFhieK_16iaF8V2aaEysiRjglrBCp1epXQmQWx3eCWrnGYJupKq2S83zH4BoE622Fu1EzMrNXXyrhjM62zONyYszHbdUVhGGjYhjMqL7qtRoByC5Mpwqj1vzFSx4e-GBxB8q5wsNLy0aw7BLP_Tg5QGjQEhaFIa93QQSC-wlSU1hOBH8FBOk4x1VUFvdRTtJDA3Q";
        //公钥
        String publickey = "-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAu/S6W53EcAwO8I2tMijf3ZYLNxTeUBIa7exekIjQr0mb38cMshjQhQooVORlVLsaz62A05cz6ReqUwdbt9JBv1iRN/wzKB+eq8u4HXQ+VFHM2vWcsezknzT8KkN9u5f2JTKRL+NXbRwnrcs7Qp/q4pIdpyjU+YBhDd1HlNqAt/SgQJGtdIbJ+TSEyMLsWdMAaiOEHeIbHLGYMyskx8o2i+2piqzt/CYoxLaFt4YpKpO9DWfPn7v5i6hN3zLijo+sUZbCWYwJTk/0zTADshRru9LuegzvhPtcSzScmi46fjZ6Q+OQn3N3mO8gN467eji3nLvuqTG5C5VR7CXtfbUnUwIDAQAB-----END PUBLIC KEY-----";

        //校验Jwt
        Jwt jwt = JwtHelper.decodeAndVerify(token, new RsaVerifier(publickey));

        //获取Jwt原始内容
        String claims = jwt.getClaims();
        System.out.println(claims);
        //jwt令牌
        String encoded = jwt.getEncoded();
        System.out.println(encoded);
    }

}
