package com.licr.graduation.service;

import com.licr.graduation.common.BusinessException;
import com.licr.graduation.dto.ActivityDtos;
import com.licr.graduation.repository.VenueRepository;
import com.licr.graduation.vo.Views;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class VenueService {
    private final VenueRepository repo;
    public VenueService(VenueRepository repo){this.repo=repo;}
    public List<Views.Venue> list(){return repo.list().stream().map(v->new Views.Venue(v.id(),v.name(),v.address(),v.sportType(),v.description())).toList();}
    @Transactional public Long create(ActivityDtos.Venue v){return repo.insert(v.name(),v.address(),v.sportType(),v.description());}
    @Transactional public void update(Long id,ActivityDtos.Venue v){if(repo.find(id).isEmpty())throw new BusinessException("场地不存在");repo.update(id,v.name(),v.address(),v.sportType(),v.description());}
    @Transactional public void delete(Long id){if(repo.find(id).isEmpty())throw new BusinessException("场地不存在");repo.disable(id);}
}
