package com.licr.graduation.service;

import com.licr.graduation.dto.AuthDtos;
import com.licr.graduation.security.CurrentUser;
import com.licr.graduation.vo.Views;

public interface AuthService {
    Views.Login register(AuthDtos.Register input);
    Views.Login login(AuthDtos.Login input);
    Views.User profile(CurrentUser current);
    Views.User updateProfile(CurrentUser current,AuthDtos.Profile input);
}
