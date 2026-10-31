package com.licr.graduation.web;
import com.licr.graduation.common.Result;import com.licr.graduation.dto.AuthDtos;import com.licr.graduation.security.CurrentUser;import com.licr.graduation.service.AuthService;import jakarta.servlet.http.HttpServletRequest;import jakarta.validation.Valid;import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/auth") public class AuthController { private final AuthService service; public AuthController(AuthService service){this.service=service;}
 @PostMapping("/register") public Result<?> register(@Valid @RequestBody AuthDtos.Register i){return Result.ok(service.register(i));}
 @PostMapping("/login") public Result<?> login(@Valid @RequestBody AuthDtos.Login i){return Result.ok(service.login(i));}
 @GetMapping("/profile") public Result<?> profile(HttpServletRequest r){return Result.ok(service.profile(user(r)));}
 @PutMapping("/profile") public Result<?> profile(HttpServletRequest r,@Valid @RequestBody AuthDtos.Profile i){return Result.ok(service.updateProfile(user(r),i));}
 static CurrentUser user(HttpServletRequest r){return (CurrentUser)r.getAttribute("currentUser");}
}
