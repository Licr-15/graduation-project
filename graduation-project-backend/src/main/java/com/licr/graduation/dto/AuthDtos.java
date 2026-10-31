package com.licr.graduation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDtos {
    private AuthDtos() {}
    public record Register(@NotBlank @Size(min=3,max=50) String username, @NotBlank @Size(min=6,max=72) String password, @NotBlank @Size(max=50) String nickname) {}
    public record Login(@NotBlank String username, @NotBlank String password) {}
    public record Profile(@NotBlank @Size(max=50) String nickname, @Size(max=20) String phone, @Size(max=255) String avatar) {}
}
