package com.licr.graduation.web;
import com.licr.graduation.common.Result;import com.licr.graduation.dto.ActivityDtos;import com.licr.graduation.security.CurrentUser;import com.licr.graduation.service.NoticeService;import com.licr.graduation.service.VenueService;import jakarta.servlet.http.HttpServletRequest;import jakarta.validation.Valid;import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/admin") public class AdminController {private final VenueService venues;private final NoticeService notices;public AdminController(VenueService venues,NoticeService notices){this.venues=venues;this.notices=notices;}
 @PostMapping("/venues") public Result<?> createVenue(@Valid @RequestBody ActivityDtos.Venue i){return Result.ok(venues.create(i));}
 @PutMapping("/venues/{id}") public Result<?> updateVenue(@PathVariable Long id,@Valid @RequestBody ActivityDtos.Venue i){venues.update(id,i);return Result.ok(null);}
 @DeleteMapping("/venues/{id}") public Result<?> deleteVenue(@PathVariable Long id){venues.delete(id);return Result.ok(null);}
 @PostMapping("/notices") public Result<?> createNotice(HttpServletRequest r,@Valid @RequestBody ActivityDtos.Notice i){return Result.ok(notices.create((CurrentUser)r.getAttribute("currentUser"),i));}
}
