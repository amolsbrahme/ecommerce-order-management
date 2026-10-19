package com.tenacious.ecommerce.inventory.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * Stock ledger row for a single product SKU. {@code availableQuantity} is
 * what can still be sold; {@code reservedQuantity} tracks quantity that has
 * been provisionally held against in-flight orders. A JPA
 * {@link Version @Version} field guards against lost updates when
 * concurrent orders race to reserve stock for the same SKU.
 */
@Entity
@Table(name = "stock")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String sku;

    @Column(name = "available_quantity", nullable = false)
    private Integer availableQuantity;

    @Column(name = "reserved_quantity", nullable = false)
    private Integer reservedQuantity;

    @Version
    private Long version;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    public boolean hasSufficientStock(int quantity) {
        return this.availableQuantity >= quantity;
    }

    public void reserve(int quantity) {
        this.availableQuantity -= quantity;
        this.reservedQuantity += quantity;
    }

    public void release(int quantity) {
        int toRelease = Math.min(quantity, this.reservedQuantity);
        this.reservedQuantity -= toRelease;
        this.availableQuantity += toRelease;
    }
}
