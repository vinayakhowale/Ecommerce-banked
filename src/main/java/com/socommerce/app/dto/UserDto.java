package com.socommerce.app.dto;

import com.socommerce.app.entity.RoleName;
import com.socommerce.app.entity.User;

public record UserDto(
        Long id,
        String name,
        String email,
        String profileImageUrl,
        String phone,
        RoleName role,
        boolean active,
        java.time.LocalDateTime createdAt
) {
    public static UserDto from(User u) {
        return new UserDto(u.getId(), u.getName(), u.getEmail(), u.getProfileImageUrl(),
                u.getPhone(), u.getRole(), u.isActive(), u.getCreatedAt());
    }
}
