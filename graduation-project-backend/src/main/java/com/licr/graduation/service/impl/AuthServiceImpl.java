package com.licr.graduation.service.impl;

import com.licr.graduation.common.BusinessException;
import com.licr.graduation.dto.AuthDtos;
import com.licr.graduation.entity.UserEntity;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.licr.graduation.mapper.UserMapper;
import com.licr.graduation.persistence.UserRow;
import com.licr.graduation.security.CurrentUser;
import com.licr.graduation.security.JwtService;
import com.licr.graduation.service.AuthService;
import com.licr.graduation.vo.Views;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {
    private final UserMapper users; private final PasswordEncoder encoder; private final JwtService jwt;
    public AuthServiceImpl(UserMapper users,PasswordEncoder encoder,JwtService jwt){this.users=users;this.encoder=encoder;this.jwt=jwt;}
    @Transactional public Views.Login register(AuthDtos.Register input){
        if(users.selectCount(new QueryWrapper<UserRow>().eq("username",input.username()))>0)throw new BusinessException("用户名已被使用");
        UserRow row=new UserRow();row.username=input.username();row.password=encoder.encode(input.password());row.nickname=input.nickname();row.role="STUDENT";row.status=1;users.insert(row);
        return loginView(entity(row));
    }
    public Views.Login login(AuthDtos.Login input){
        UserRow row=users.selectOne(new QueryWrapper<UserRow>().eq("username",input.username()));
        if(row==null)throw new BusinessException("用户名或密码错误");
        UserEntity user=entity(row);
        if(user.status()!=1||!encoder.matches(input.password(),user.password()))throw new BusinessException("用户名或密码错误");
        return loginView(user);
    }
    public Views.User profile(CurrentUser current){UserRow row=users.selectById(current.id());if(row==null)throw new BusinessException("用户不存在");return view(entity(row));}
    @Transactional public Views.User updateProfile(CurrentUser current,AuthDtos.Profile input){users.update(null,new UpdateWrapper<UserRow>().eq("id",current.id()).set("nickname",input.nickname()).set("phone",input.phone()).set("avatar",input.avatar()));return profile(current);}
    private Views.Login loginView(UserEntity u){return new Views.Login(jwt.create(new CurrentUser(u.id(),u.username(),u.role())),view(u));}
    private Views.User view(UserEntity u){return new Views.User(u.id(),u.username(),u.nickname(),u.phone(),u.avatar(),u.role());}
    private UserEntity entity(UserRow row){return new UserEntity(row.id,row.username,row.password,row.nickname,row.phone,row.avatar,row.role,row.status,row.createdAt);}
}
