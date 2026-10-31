package com.licr.graduation.web;
import com.licr.graduation.common.Result;import org.springframework.web.bind.annotation.*;import java.util.Map;
@RestController @RequestMapping("/api/system") public class SystemController {@GetMapping("/health") public Result<?> health(){return Result.ok(Map.of("service","graduation-project-backend","status","UP"));}}
