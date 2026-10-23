package com.socommerce.app.repository;

import com.socommerce.app.entity.Influencer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InfluencerRepository extends JpaRepository<Influencer, Long> {
    Optional<Influencer> findByCodeIgnoreCase(String code);
    boolean existsByCodeIgnoreCase(String code);
    Page<Influencer> findByActiveTrue(Pageable pageable);

    /** A creator signs in with the same email the admin saved on their influencer profile. */
    java.util.Optional<Influencer> findFirstByEmailIgnoreCaseAndActiveTrue(String email);
}
