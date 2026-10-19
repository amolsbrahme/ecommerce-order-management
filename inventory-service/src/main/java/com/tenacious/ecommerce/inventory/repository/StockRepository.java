package com.tenacious.ecommerce.inventory.repository;

import com.tenacious.ecommerce.inventory.model.Stock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StockRepository extends JpaRepository<Stock, Long> {

    Optional<Stock> findBySku(String sku);

    boolean existsBySku(String sku);

    /**
     * Pessimistic write lock on the row while a reserve/release operation
     * reads-then-writes it, on top of the optimistic {@code @Version}
     * check - belt and braces under concurrent order placement for the
     * same SKU.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Stock s where s.sku = :sku")
    Optional<Stock> findBySkuForUpdate(@Param("sku") String sku);
}
