package com.licr.graduation.config;

import com.licr.graduation.security.JwtInterceptor;
import com.licr.graduation.security.RoleInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final JwtInterceptor jwt; private final RoleInterceptor role;
    public WebConfig(JwtInterceptor jwt, RoleInterceptor role) { this.jwt = jwt; this.role = role; }
    @Override public void addInterceptors(InterceptorRegistry registry) {
        // JWT 拦截器覆盖所有 API，由拦截器根据请求方法和路径精确放行公开接口。
        registry.addInterceptor(jwt).addPathPatterns("/api/**");
        registry.addInterceptor(role).addPathPatterns("/api/admin/**");
    }
}
