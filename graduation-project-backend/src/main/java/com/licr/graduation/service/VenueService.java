package com.licr.graduation.service;

import com.licr.graduation.dto.ActivityDtos;
import com.licr.graduation.vo.Views;
import java.util.List;

public interface VenueService {
    List<Views.Venue> list();
    Long create(ActivityDtos.Venue input);
    void update(Long id,ActivityDtos.Venue input);
    void delete(Long id);
}
