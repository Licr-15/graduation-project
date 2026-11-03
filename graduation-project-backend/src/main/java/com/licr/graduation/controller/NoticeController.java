package com.licr.graduation.controller;
import com.licr.graduation.common.Result;import com.licr.graduation.dto.ActivityDtos;import com.licr.graduation.service.NoticeService;import jakarta.servlet.http.HttpServletRequest;import jakarta.validation.Valid;import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/notices") public class NoticeController {private final NoticeService service;public NoticeController(NoticeService service){this.service=service;}
 @GetMapping public Result<?> list(){return Result.ok(service.list());}
}
