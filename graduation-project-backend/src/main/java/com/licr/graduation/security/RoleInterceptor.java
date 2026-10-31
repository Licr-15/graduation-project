package com.licr.graduation.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.licr.graduation.common.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import java.nio.charset.StandardCharsets;

@Component
public class RoleInterceptor implements HandlerInterceptor {
    private final ObjectMapper mapper;
    public RoleInterceptor(ObjectMapper mapper) { this.mapper = mapper; }
    @Override public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        CurrentUser user = (CurrentUser) request.getAttribute("currentUser");
        if (user != null && "ADMIN".equals(user.role())) return true;
        response.setStatus(403); response.setCharacterEncoding(StandardCharsets.UTF_8.name()); response.setContentType("application/json;charset=UTF-8");
        mapper.writeValue(response.getWriter(), Result.fail("需要管理员权限")); return false;
    }
}
