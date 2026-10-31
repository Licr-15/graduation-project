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
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) { write(response, "请先登录"); return false; }
        try { request.setAttribute("currentUser", jwtService.parse(header.substring(7))); return true; }
        catch (JwtException | IllegalArgumentException e) { write(response, "登录凭证无效或已过期"); return false; }
    }
    private void write(HttpServletResponse response, String message) throws Exception {
        response.setStatus(401); response.setCharacterEncoding(StandardCharsets.UTF_8.name()); response.setContentType("application/json;charset=UTF-8");
        mapper.writeValue(response.getWriter(), Result.fail(message));
    }
}
