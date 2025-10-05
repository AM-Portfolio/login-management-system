package com.modernportfolio.autoconfigure;

import com.modernportfolio.configuration.JwtAuthenticationEntryPoint;
import com.modernportfolio.util.JwtUtil;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

/**
 * Auto-configuration for JWT token validation in microservices.
 *
 * This configuration provides:
 * - JwtUtil: For JWT token parsing and validation
 * - JwtAuthenticationEntryPoint: For handling authentication errors
 *
 * It explicitly EXCLUDES user management components:
 * - JwtService, UserService, RoleService (require database)
 * - JwtRequestFilter, WebSecurityConfiguration (use custom implementations)
 *
 * Usage in target microservice:
 * <pre>
 * {@code
 * @SpringBootApplication
 * @Import(JwtValidationAutoConfiguration.class)
 * public class YourApplication {
 *     // Your application code
 * }
 * }
 * </pre>
 *
 * @author Auto-generated
 * @since 1.0
 */
@AutoConfiguration
@ComponentScan(
    basePackages = {
        "com.modernportfolio.util",
        "com.modernportfolio.configuration"
    },
    excludeFilters = {
        @ComponentScan.Filter(
            type = FilterType.REGEX,
            pattern = "com\\.modernportfolio\\.configuration\\.WebSecurityConfiguration"
        ),
        @ComponentScan.Filter(
            type = FilterType.REGEX,
            pattern = "com\\.modernportfolio\\.configuration\\.JwtRequestFilter"
        ),
        @ComponentScan.Filter(
            type = FilterType.REGEX,
            pattern = "com\\.modernportfolio\\.configuration\\.CorsConfiguration"
        ),
        @ComponentScan.Filter(
            type = FilterType.REGEX,
            pattern = "com\\.modernportfolio\\.service\\..*"
        )
    }
)
public class JwtValidationAutoConfiguration {

    /**
     * This configuration automatically registers:
     * - JwtUtil: JWT token utility for parsing and validation
     * - JwtAuthenticationEntryPoint: Authentication error handler
     *
     * Note: Target microservices must provide their own:
     * - Custom JWT filter (e.g., MarketDataJwtRequestFilter)
     * - SecurityConfig with SecurityFilterChain
     */
}

