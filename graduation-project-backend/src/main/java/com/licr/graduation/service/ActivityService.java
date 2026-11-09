package com.licr.graduation.service;

import com.licr.graduation.dto.ActivityDtos;
import com.licr.graduation.security.CurrentUser;
import com.licr.graduation.vo.Views;
import java.util.List;

public interface ActivityService {
    List<Views.Activity> search(String keyword,String sportType,int page,int size);
    List<Views.Activity> mine(CurrentUser user);
    List<Views.Activity> joined(CurrentUser user);
    List<Views.Activity> all();
    Views.Activity detail(Long id);
    Long create(CurrentUser user,ActivityDtos.Save input);
    void update(CurrentUser user,Long id,ActivityDtos.Save input);
    void delete(CurrentUser user,Long id);
    void adminUpdate(Long id,ActivityDtos.Save input);
    void adminDelete(Long id);
    void join(CurrentUser user,Long id);
    void leave(CurrentUser user,Long id);
}
