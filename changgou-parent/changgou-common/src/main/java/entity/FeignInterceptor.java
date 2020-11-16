package entity;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

/**
 *  将所有请求头信息保存(feign调用)-(服务之间调用)
 */
public class FeignInterceptor implements RequestInterceptor {
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
    }
}
