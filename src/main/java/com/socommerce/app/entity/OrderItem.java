package com.socommerce.app.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * One line of an order. Every customer-visible field is a snapshot taken at checkout, so a
 * later price change or product rename never alters what an old invoice says.
 */
@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    /** Kept for reporting. Null if the product is later deleted -- the snapshot still stands. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(nullable = false)
    private String productName;

    @Column(length = 2048)
    private String productImageUrl;

    @Column(length = 120)
    private String brand;

    @Column(nullable = false)
    private int quantity;

    /** Price actually charged per unit, after any discount. */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    /** List price before discount, kept so the invoice can show the saving. */
    @Column(precision = 12, scale = 2)
    private BigDecimal mrp;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal lineTotal;
}
