package com.autoflow.support;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base class that starts a real PostgreSQL database in a container for integration
 * and repository tests. We never use H2 as a stand-in for PostgreSQL.
 *
 * <p>The container is static so it is started once and shared by all subclasses.
 * {@code @ServiceConnection} wires Spring's datasource to the container automatically.</p>
 */
@Testcontainers
public abstract class PostgresContainerSupport {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine");
}
