package com.licr.graduation.web;
import com.licr.graduation.common.Result;import com.licr.graduation.dto.ActivityDtos;import com.licr.graduation.service.ActivityService;import jakarta.servlet.http.HttpServletRequest;import jakarta.validation.Valid;import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/activities") public class ActivityController {private final ActivityService service;public ActivityController(ActivityService service){this.service=service;}
 @GetMapping public Result<?> search(@RequestParam(required=false)String keyword,@RequestParam(required=false)String sportType,@RequestParam(defaultValue="1")int page,@RequestParam(defaultValue="10")int size){return Result.ok(service.search(keyword,sportType,page,size));}
 @PostMapping public Result<?> create(HttpServletRequest r,@Valid @RequestBody ActivityDtos.Save i){return Result.ok(service.create(AuthController.user(r),i));}
 @PutMapping("/{id}") public Result<?> update(HttpServletRequest r,@PathVariable Long id,@Valid @RequestBody ActivityDtos.Save i){service.update(AuthController.user(r),id,i);return Result.ok(null);}
 @DeleteMapping("/{id}") public Result<?> delete(HttpServletRequest r,@PathVariable Long id){service.delete(AuthController.user(r),id);return Result.ok(null);}
 @PostMapping("/{id}/join") public Result<?> join(HttpServletRequest r,@PathVariable Long id){service.join(AuthController.user(r),id);return Result.ok(null);}
 @DeleteMapping("/{id}/join") public Result<?> leave(HttpServletRequest r,@PathVariable Long id){service.leave(AuthController.user(r),id);return Result.ok(null);}
 @GetMapping("/mine") public Result<?> mine(HttpServletRequest r){return Result.ok(service.mine(AuthController.user(r)));}
}
