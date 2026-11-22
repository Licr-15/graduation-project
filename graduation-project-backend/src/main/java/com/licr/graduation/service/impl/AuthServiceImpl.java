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
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.regex.Pattern;

@Service
public class AuthServiceImpl implements AuthService {
    private static final Pattern PHONE = Pattern.compile("^1[3-9]\\d{9}$");
    private static final Pattern EMAIL = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", Pattern.CASE_INSENSITIVE);
    private final UserMapper users; private final PasswordEncoder encoder; private final JwtService jwt;
    public AuthServiceImpl(UserMapper users,PasswordEncoder encoder,JwtService jwt){this.users=users;this.encoder=encoder;this.jwt=jwt;}
    @Transactional public Views.Login register(AuthDtos.Register input){
        String username=input.username().trim(); Contact contact=parseContact(input.contact());
        if(users.selectCount(new QueryWrapper<UserRow>().eq("username",username))>0)throw new BusinessException("用户名已被使用");
        ensureContactAvailable(contact,null);
        UserRow row=new UserRow();row.username=username;row.password=encoder.encode(input.password());row.nickname=input.nickname().trim();row.phone=contact.phone();row.email=contact.email();row.role="STUDENT";row.status=1;
        try { users.insert(row); } catch (DuplicateKeyException e) { throw new BusinessException("用户名、手机号或邮箱已被使用"); }
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
    @Transactional public Views.User updateProfile(CurrentUser current,AuthDtos.Profile input){
        String phone=normalizeOptional(input.phone()); String email=normalizeEmail(input.email());
        if(phone!=null&&!PHONE.matcher(phone).matches())throw new BusinessException("请输入有效的中国大陆手机号");
        if(email!=null&&!EMAIL.matcher(email).matches())throw new BusinessException("请输入有效的邮箱地址");
        ensureContactAvailable(new Contact(phone,email),current.id());
        try { users.update(null,new UpdateWrapper<UserRow>().eq("id",current.id()).set("nickname",input.nickname().trim()).set("phone",phone).set("email",email).set("avatar",input.avatar())); }
        catch (DuplicateKeyException e) { throw new BusinessException("手机号或邮箱已被其他账号使用"); }
        return profile(current);
    }
    private Views.Login loginView(UserEntity u){return new Views.Login(jwt.create(new CurrentUser(u.id(),u.username(),u.role())),view(u));}
    private Views.User view(UserEntity u){return new Views.User(u.id(),u.username(),u.nickname(),u.phone(),u.email(),u.avatar(),u.role());}
    private UserEntity entity(UserRow row){return new UserEntity(row.id,row.username,row.password,row.nickname,row.phone,row.email,row.avatar,row.role,row.status,row.createdAt);}
    private Contact parseContact(String raw){
        String value=raw==null?"":raw.trim();
        if(PHONE.matcher(value).matches())return new Contact(value,null);
        String email=normalizeEmail(value);
        if(email!=null&&EMAIL.matcher(email).matches())return new Contact(null,email);
        throw new BusinessException("请输入有效的中国大陆手机号或邮箱地址");
    }
    private String normalizeOptional(String value){return value==null||value.isBlank()?null:value.trim();}
    private String normalizeEmail(String value){String normalized=normalizeOptional(value);return normalized==null?null:normalized.toLowerCase(Locale.ROOT);}
    private void ensureContactAvailable(Contact contact,Long currentId){
        if(contact.phone()!=null){QueryWrapper<UserRow> query=new QueryWrapper<UserRow>().eq("phone",contact.phone());if(currentId!=null)query.ne("id",currentId);if(users.selectCount(query)>0)throw new BusinessException("该手机号已被其他账号使用");}
        if(contact.email()!=null){QueryWrapper<UserRow> query=new QueryWrapper<UserRow>().eq("email",contact.email());if(currentId!=null)query.ne("id",currentId);if(users.selectCount(query)>0)throw new BusinessException("该邮箱已被其他账号使用");}
    }
    private record Contact(String phone,String email) {}
}
