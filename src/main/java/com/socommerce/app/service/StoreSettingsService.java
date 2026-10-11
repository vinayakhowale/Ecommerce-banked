package com.socommerce.app.service;

import com.socommerce.app.dto.StoreSettingsDto;
import com.socommerce.app.entity.StoreSettings;
import com.socommerce.app.repository.StoreSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StoreSettingsService {

    /** The single settings row lives at this id. */
    private static final Long SINGLETON_ID = 1L;

    private final StoreSettingsRepository repository;

    /**
     * Returns the settings, creating them with defaults the first time they are asked for.
     * Writing on a read keeps a fresh database from ever serving a nameless storefront.
     */
    @Transactional
    public StoreSettings get() {
        return repository.findById(SINGLETON_ID).orElseGet(this::createDefaults);
    }

    @Transactional
    public StoreSettings update(StoreSettingsDto.Request req) {
        StoreSettings settings = get();
        settings.setStoreName(req.storeName().trim());
        settings.setTagline(trimToNull(req.tagline()));
        settings.setSupportEmail(trimToNull(req.supportEmail()));
        settings.setSupportPhone(trimToNull(req.supportPhone()));
        settings.setSupportHours(trimToNull(req.supportHours()));

        // Replace in place: the list is an @ElementCollection, so Hibernate must see the mutation.
        settings.getAnnouncements().clear();
        if (req.announcements() != null) {
            req.announcements().stream()
                    .map(this::trimToNull)
                    .filter(java.util.Objects::nonNull)
                    .limit(6)
                    .forEach(settings.getAnnouncements()::add);
        }
        return repository.save(settings);
    }

    private StoreSettings createDefaults() {
        List<String> notes = new ArrayList<>(List.of(
                "Free shipping on orders over ₹999",
                "Easy 7-day returns",
                "Secure payments with Razorpay"
        ));
        StoreSettings settings = StoreSettings.builder()
                .id(SINGLETON_ID)
                .storeName("Trendly")
                .tagline("Products worn and recommended by the creators you already follow.")
                .announcements(notes)
                .supportHours("10am–7pm, Mon–Fri")
                .build();
        return repository.save(settings);
    }

    private String trimToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}
