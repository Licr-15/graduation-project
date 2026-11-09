package com.licr.graduation.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public final class ActivityDtos {
    private ActivityDtos() {}
    public record Save(@NotBlank @Size(max=100) String title, @NotBlank @Size(max=50) String sportType, Long venueId, @Size(max=1000) String description, @Size(max=1000) String imageUrl, @NotNull LocalDateTime startTime, @NotNull LocalDateTime endTime, @Min(2) @Max(100) int maxPeople) {}
    public record Comment(@NotBlank @Size(max=500) String content) {}
    public record Venue(@NotBlank @Size(max=100) String name, @NotBlank @Size(max=255) String address, @NotBlank @Size(max=50) String sportType, @Size(max=500) String description) {}
    public record Notice(@NotBlank @Size(max=100) String title, @NotBlank String content) {}
}
