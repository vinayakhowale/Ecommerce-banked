package com.socommerce.app.repository;

import com.socommerce.app.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {

    List<Address> findByUserIdOrderByDefaultAddressDescUpdatedAtDesc(Long userId);

    Optional<Address> findByIdAndUserId(Long id, Long userId);

    /** Clears the default flag across a user's addresses before a new one is set. */
    @Modifying
    @Query("update Address a set a.defaultAddress = false where a.user.id = :userId")
    void clearDefaultFor(@Param("userId") Long userId);

    long countByUserId(Long userId);
}
