package com.socommerce.app.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * A delivery address in the customer's address book. An order never points at this row --
 * it copies the fields at checkout, so editing an address later cannot rewrite past orders.
 */
@Entity
@Table(name = "addresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotBlank
    @Column(nullable = false)
    private String fullName;

    @NotBlank
    @Pattern(regexp = "^[0-9+\\-\\s()]{7,20}$")
    @Column(nullable = false, length = 20)
    private String phone;

    @NotBlank
    @Column(nullable = false, length = 255)
    private String line1;

    @Column(length = 255)
    private String line2;

    @Column(length = 120)
    private String landmark;

    @NotBlank
    @Column(nullable = false, length = 120)
    private String city;

    @NotBlank
    @Column(nullable = false, length = 120)
    private String state;

    @NotBlank
    @Pattern(regexp = "^[1-9][0-9]{5}$", message = "Enter a valid 6-digit pincode")
    @Column(nullable = false, length = 10)
    private String pincode;

    /** Home, Work, or whatever the customer calls it. */
    @Column(length = 40)
    private String label;

    /** Pre-selected at checkout. Exactly one address per user carries this. */
    @Builder.Default
    @Column(nullable = false)
    private boolean defaultAddress = false;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
