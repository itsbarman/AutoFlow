package com.autoflow.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Enables automatic population of {@code @CreatedDate} and {@code @LastModifiedDate}
 * fields on entities. This is why we never trust the client to send createdAt/updatedAt.
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
