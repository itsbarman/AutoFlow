package com.autoflow.security;

/**
 * Application roles. Stored as text in the {@code user_roles} table and exposed
 * to Spring Security as authorities prefixed with {@code ROLE_}.
 */
public enum Role {
    ADMIN,
    MECHANIC,
    RECEPTIONIST
}
