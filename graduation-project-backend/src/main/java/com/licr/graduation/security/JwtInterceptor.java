package com.licr.graduation.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.licr.graduation.common.Result;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import java.nio.charset.StandardCharsets;

@Component
public class JwtInterceptor implements HandlerInterceptor {
    private final JwtService jwtService; private final ObjectMapper mapper;
    public JwtInterceptor(JwtService jwtService, ObjectMapper mapper) { this.jwtService = jwtService; this.mapper = mapper; }
    @Override public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;
        if (isPublic(request)) return true;
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) { write(response, "请先登录"); return false; }
        try { request.setAttribute("currentUser", jwtService.parse(header.substring(7))); return true; }
        catch (JwtException | IllegalArgumentException e) { write(response, "登录凭证无效或已过期"); return false; }
    }
    /** 只放行明确的公开接口，避免把个人资料或我的活动误设为免登录。 */
    private boolean isPublic(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        String method = request.getMethod();
        if (("POST".equals(method) && ("/api/auth/register".equals(path) || "/api/auth/login".equals(path)))
                || ("GET".equals(method) && ("/api/system/health".equals(path)
                || "/api/venues".equals(path) || "/api/activities".equals(path)
                || "/api/notices".equals(path) || path.matches("/api/comments/activity/\\d+")
                || path.matches("/api/activities/\\d+")))) return true;
        return false;
    }
    private void write(HttpServletResponse response, String message) throws Exception {
        response.setStatus(401); response.setCharacterEncoding(StandardCharsets.UTF_8.name()); response.setContentType("application/json;charset=UTF-8");
        mapper.writeValue(response.getWriter(), Result.fail(message));
    }
}
