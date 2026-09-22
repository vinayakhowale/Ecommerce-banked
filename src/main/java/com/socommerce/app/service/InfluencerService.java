package com.socommerce.app.service;

import com.socommerce.app.dto.InfluencerDto;
import com.socommerce.app.entity.Influencer;
import com.socommerce.app.exception.BadRequestException;
import com.socommerce.app.exception.ResourceNotFoundException;
import com.socommerce.app.repository.InfluencerDiscountRepository;
import com.socommerce.app.repository.InfluencerRepository;
import com.socommerce.app.repository.InfluencerSaleRepository;
import com.socommerce.app.repository.PaymentOrderRepository;
import com.socommerce.app.repository.ProductClickRepository;
import com.socommerce.app.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InfluencerService {

    private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // no O/0/I/1 ambiguity
    private static final SecureRandom RANDOM = new SecureRandom();

    private final InfluencerRepository influencerRepository;
    private final ProductClickRepository productClickRepository;
    private final PostRepository postRepository;
    private final InfluencerDiscountRepository influencerDiscountRepository;
    private final InfluencerSaleRepository influencerSaleRepository;
    private final PaymentOrderRepository paymentOrderRepository;

    @Transactional(readOnly = true)
    public Page<Influencer> list(Pageable pageable) {
        return influencerRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Influencer getAny(Long id) {
        return influencerRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Influencer not found"));
    }

    @Transactional(readOnly = true)
    public Optional<Influencer> findActiveByCode(String code) {
        if (code == null || code.isBlank()) return Optional.empty();
        return influencerRepository.findByCodeIgnoreCase(code.trim()).filter(Influencer::isActive);
    }

    @Transactional
    public Influencer create(InfluencerDto.Request req) {
        String code = (req.code() == null || req.code().isBlank())
                ? generateUniqueCode(req.name())
                : normalizeAndValidateCode(req.code());

        Influencer influencer = Influencer.builder()
                .name(req.name())
                .code(code)
                .profileImageUrl(req.profileImageUrl())
                .email(req.email())
                .phone(req.phone())
                .active(req.active() == null || req.active())
                .build();
        return influencerRepository.save(influencer);
    }

    @Transactional
    public Influencer update(Long id, InfluencerDto.Request req) {
        Influencer influencer = getAny(id);
        influencer.setName(req.name());
        if (req.code() != null && !req.code().isBlank() && !req.code().equalsIgnoreCase(influencer.getCode())) {
            influencer.setCode(normalizeAndValidateCode(req.code()));
        }
        influencer.setProfileImageUrl(req.profileImageUrl());
        influencer.setEmail(req.email());
        influencer.setPhone(req.phone());
        if (req.active() != null) influencer.setActive(req.active());
        return influencerRepository.save(influencer);
    }

    @Transactional
    public void setActive(Long id, boolean active) {
        Influencer influencer = getAny(id);
        influencer.setActive(active);
        influencerRepository.save(influencer);
    }

    /**
     * Sets the admin-maintained "official" sales count to an absolute value -- e.g. "update to 90"
     * after confirming 10 more units sold. This does not touch InfluencerSale transaction records;
     * it's the standalone manually-maintained total described in the Influencer Sales Management
     * requirement, kept deliberately simple (a single editable number) rather than derived from a
     * running log.
     */
    @Transactional
    public Influencer setSalesCount(Long id, int salesCount) {
        Influencer influencer = getAny(id);
        influencer.setSalesCount(salesCount);
        return influencerRepository.save(influencer);
    }

    @Transactional
    public void delete(Long id) {
        Influencer influencer = getAny(id);
        // Unlink from any posts and clicks that reference this influencer, and remove any
        // discount tier / confirmed-sale records tied to them, rather than blocking the delete --
        // consistent with how Product/Post deletes behave elsewhere in this app.
        postRepository.findAll().stream()
                .filter(p -> p.getInfluencer() != null && p.getInfluencer().getId().equals(id))
                .forEach(p -> p.setInfluencer(null));
        productClickRepository.deleteByInfluencerId(id);
        influencerDiscountRepository.deleteByInfluencerId(id);
        influencerSaleRepository.deleteByInfluencerId(id);
        paymentOrderRepository.detachFromInfluencer(id);
        influencerRepository.delete(influencer);
    }

    private String normalizeAndValidateCode(String rawCode) {
        String code = rawCode.trim().toUpperCase().replaceAll("[^A-Z0-9]", "");
        if (code.isBlank()) {
            throw new BadRequestException("Code must contain at least one letter or digit");
        }
        if (influencerRepository.existsByCodeIgnoreCase(code)) {
            throw new BadRequestException("Code '" + code + "' is already in use by another influencer");
        }
        return code;
    }

    /** Generates a short, human-shareable, guaranteed-unique code like "RIYA482". */
    private String generateUniqueCode(String name) {
        String base = name.replaceAll("[^A-Za-z]", "").toUpperCase();
        base = base.length() > 5 ? base.substring(0, 5) : base;
        if (base.isBlank()) base = "CODE";

        for (int attempt = 0; attempt < 20; attempt++) {
            StringBuilder suffix = new StringBuilder();
            for (int i = 0; i < 3; i++) {
                suffix.append(CODE_CHARS.charAt(RANDOM.nextInt(CODE_CHARS.length())));
            }
            String candidate = base + suffix;
            if (!influencerRepository.existsByCodeIgnoreCase(candidate)) {
                return candidate;
            }
        }
        // Extremely unlikely fallback: timestamp-based suffix guarantees termination.
        return base + System.currentTimeMillis() % 100000;
    }
}
