package com.licr.graduation.controller;
import com.licr.graduation.common.Result;import com.licr.graduation.dto.ActivityDtos;import com.licr.graduation.service.CommentService;import jakarta.servlet.http.HttpServletRequest;import jakarta.validation.Valid;import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/comments") public class CommentController {private final CommentService service;public CommentController(CommentService service){this.service=service;}
 @GetMapping("/activity/{activityId}") public Result<?> list(@PathVariable Long activityId){return Result.ok(service.list(activityId));}
 @PostMapping("/activity/{activityId}") public Result<?> create(HttpServletRequest r,@PathVariable Long activityId,@Valid @RequestBody ActivityDtos.Comment i){return Result.ok(service.create(AuthController.user(r),activityId,i));}
 @DeleteMapping("/{id}") public Result<?> delete(HttpServletRequest r,@PathVariable Long id){service.delete(AuthController.user(r),id);return Result.ok(null);}
}
