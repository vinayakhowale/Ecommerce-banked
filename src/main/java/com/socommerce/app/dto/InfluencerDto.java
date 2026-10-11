package com.socommerce.app.dto;

import com.socommerce.app.entity.Influencer;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class InfluencerDto {

    public record Request(
            @NotBlank String name,
            String code,
            String profileImageUrl,
            String email,
            String phone,
            Boolean active
    ) {}

    /** Body for the dedicated "set the official sales count" admin action. */
    public record SalesCountRequest(@Min(0) int salesCount) {}

    public record Response(
            Long id, String name, String code, String profileImageUrl,
            String email, String phone, boolean active, int salesCount,
            java.time.LocalDateTime createdAt
    ) {
        public static Response from(Influencer i) {
            return new Response(i.getId(), i.getName(), i.getCode(), i.getProfileImageUrl(),
                    i.getEmail(), i.getPhone(), i.isActive(), i.getSalesCount(), i.getCreatedAt());
        }
    }
}
