package com.licr.graduation.service.impl;

import com.licr.graduation.common.BusinessException;
import com.licr.graduation.dto.ActivityDtos;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.licr.graduation.mapper.VenueMapper;
import com.licr.graduation.persistence.SportVenueRow;
import com.licr.graduation.service.VenueService;
import com.licr.graduation.vo.Views;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class VenueServiceImpl implements VenueService {
    private final VenueMapper mapper;
    public VenueServiceImpl(VenueMapper mapper){this.mapper=mapper;}
    public List<Views.Venue> list(){return mapper.selectList(new QueryWrapper<SportVenueRow>().eq("status",1).orderByDesc("id")).stream().map(v->new Views.Venue(v.id,v.name,v.address,v.sportType,v.description)).toList();}
    @Transactional public Long create(ActivityDtos.Venue v){SportVenueRow row=new SportVenueRow();row.name=v.name();row.address=v.address();row.sportType=v.sportType();row.description=v.description();row.status=1;mapper.insert(row);return row.id;}
    @Transactional public void update(Long id,ActivityDtos.Venue v){if(mapper.selectById(id)==null)throw new BusinessException("场地不存在");mapper.update(null,new UpdateWrapper<SportVenueRow>().eq("id",id).set("name",v.name()).set("address",v.address()).set("sport_type",v.sportType()).set("description",v.description()));}
    @Transactional public void delete(Long id){if(mapper.selectById(id)==null)throw new BusinessException("场地不存在");mapper.update(null,new UpdateWrapper<SportVenueRow>().eq("id",id).set("status",0));}
}
