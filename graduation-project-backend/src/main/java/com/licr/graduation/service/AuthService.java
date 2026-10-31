package com.licr.graduation.service;

import com.licr.graduation.common.BusinessException;
import com.licr.graduation.dto.AuthDtos;
import com.licr.graduation.entity.UserEntity;
import com.licr.graduation.repository.UserRepository;
import com.licr.graduation.security.CurrentUser;
import com.licr.graduation.security.JwtService;
import com.licr.graduation.vo.Views;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository users; private final PasswordEncoder encoder; private final JwtService jwt;
    public AuthService(UserRepository users,PasswordEncoder encoder,JwtService jwt){this.users=users;this.encoder=encoder;this.jwt=jwt;}
    @Transactional public Views.Login register(AuthDtos.Register input){
        if(users.findByUsername(input.username()).isPresent())throw new BusinessException("用户名已被使用");
        long id=users.insert(input.username(),encoder.encode(input.password()),input.nickname());
        UserEntity user=users.findById(id).orElseThrow(()->new BusinessException("注册失败"));
        return loginView(user);
    }
    public Views.Login login(AuthDtos.Login input){
        UserEntity user=users.findByUsername(input.username()).orElseThrow(()->new BusinessException("用户名或密码错误"));
        if(user.status()!=1||!encoder.matches(input.password(),user.password()))throw new BusinessException("用户名或密码错误");
        return loginView(user);
    }
    public Views.User profile(CurrentUser current){return users.findById(current.id()).map(this::view).orElseThrow(()->new BusinessException("用户不存在"));}
    @Transactional public Views.User updateProfile(CurrentUser current,AuthDtos.Profile input){users.updateProfile(current.id(),input.nickname(),input.phone(),input.avatar());return profile(current);}
    private Views.Login loginView(UserEntity u){return new Views.Login(jwt.create(new CurrentUser(u.id(),u.username(),u.role())),view(u));}
    private Views.User view(UserEntity u){return new Views.User(u.id(),u.username(),u.nickname(),u.phone(),u.avatar(),u.role());}
}
