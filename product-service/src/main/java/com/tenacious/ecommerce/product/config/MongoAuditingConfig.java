package com.tenacious.ecommerce.product.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

/**
 * Enables population of the {@code @CreatedDate}/{@code @LastModifiedDate}
 * fields on {@link com.tenacious.ecommerce.product.model.Product} automatically.
 */
@Configuration
@EnableMongoAuditing
public class MongoAuditingConfig {
}
