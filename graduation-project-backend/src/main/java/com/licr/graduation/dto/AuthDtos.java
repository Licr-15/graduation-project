package com.licr.graduation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDtos {
    private AuthDtos() {}
    public record Register(@NotBlank @Size(min=3,max=50) String username, @NotBlank @Size(min=6,max=72) String password, @NotBlank @Size(max=50) String nickname, @NotBlank @Size(max=254) String contact) {}
    public record Login(@NotBlank String username, @NotBlank String password) {}
    public record Profile(@NotBlank @Size(max=50) String nickname, @Size(max=20) String phone, @Size(max=254) String email, @Size(max=2800000) String avatar) {}
}
