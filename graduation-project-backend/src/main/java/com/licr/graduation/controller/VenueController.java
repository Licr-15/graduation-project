package com.licr.graduation.controller;
import com.licr.graduation.common.Result;import com.licr.graduation.service.VenueService;import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/venues") public class VenueController {private final VenueService service;public VenueController(VenueService service){this.service=service;}
 @GetMapping public Result<?> list(){return Result.ok(service.list());}
}
