package com.socommerce.app.dto;

import com.socommerce.app.entity.StoreSettings;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public class StoreSettingsDto {

    public record Request(
            @NotBlank(message = "The store name cannot be empty")
            @Size(max = 60, message = "Keep the store name under 60 characters")
            String storeName,
            @Size(max = 160) String tagline,
            List<String> announcements,
            @Size(max = 160) String supportEmail,
            @Size(max = 40) String supportPhone,
            @Size(max = 120) String supportHours
    ) {}

    public record Response(
            String storeName,
            String tagline,
            List<String> announcements,
            String supportEmail,
            String supportPhone,
            String supportHours
    ) {
        public static Response from(StoreSettings s) {
            return new Response(
                    s.getStoreName(), s.getTagline(),
                    s.getAnnouncements() == null ? List.of() : List.copyOf(s.getAnnouncements()),
                    s.getSupportEmail(), s.getSupportPhone(), s.getSupportHours());
        }
    }
}
