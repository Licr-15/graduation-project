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
        registry.addInterceptor(jwt).addPathPatterns("/api/**").excludePathPatterns("/api/auth/**", "/api/venues/**", "/api/activities", "/api/activities/*", "/api/notices/**", "/api/system/**");
        registry.addInterceptor(role).addPathPatterns("/api/admin/**");
    }
}
