# Integration Guide: Add JWT Authentication to Your Existing am-market-data Project

## Prerequisites
1. You have an existing am-market-data project at D:\am\am-market-data
2. login-management-system JARs are built and available in your local Maven repository

## Step 1: Add SINGLE Dependency to Your am-market-data/pom.xml

Add this **ONE** dependency to your EXISTING pom.xml file:

```xml
<!-- JWT Authentication from login-management-system - SINGLE JAR -->
<dependency>
    <groupId>com.modernportfolio</groupId>
    <artifactId>jwt-auth-starter</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

That's it! This single JAR includes:
- All JWT utilities (JwtUtil)
- JWT Authentication Entry Point (JwtAuthenticationEntryPoint)
- Auto-configuration for JWT validation
- All required dependencies (jjwt, spring-security, etc.)

**Note:** User management components (JwtService, UserService, RoleService) are automatically excluded - you only get token validation capabilities.

## Step 2: Add JWT Configuration to Your application.properties

Add this to your EXISTING application.properties file:

```properties
# Server Configuration (choose a different port if 8080 is in use)
server.port=8081

# Application Name
spring.application.name=market-data-service

# JWT Configuration (MUST match login-management-system)
jwt.secret=g4ASf2sEJk08Y3GoiHKdF0F78E2Vj34S+KmN1IJdF2ncxEh0bxyT1XQEPYTc7SQvqO+3wd9MC8X7S6nG0Rb0TQ==

# JWT Logging
logging.level.com.modernportfolio=DEBUG
logging.level.com.am.marketdata=DEBUG
```

## Step 3: Update Your Main Application Class

Update your EXISTING AmMarketDataApplication.java with a SINGLE import:

```java
package com.am.marketdata;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import com.modernportfolio.autoconfigure.JwtValidationAutoConfiguration;

@SpringBootApplication
@Import(JwtValidationAutoConfiguration.class)  // Single line JWT integration!
@ComponentScan(basePackages = {
    "com.am.marketdata"           // Your existing package
})
public class AmMarketDataApplication {
    public static void main(String[] args) {
        SpringApplication.run(AmMarketDataApplication.class, args);
    }
}
```

**That's it!** No need for multiple @ComponentScan annotations. The `JwtValidationAutoConfiguration` handles everything automatically.

## Step 4: Create Custom JWT Filter for Your Project

Since your project only validates tokens (doesn't create them), create a lightweight JWT filter:

Location: `src/main/java/com/am/marketdata/config/MarketDataJwtRequestFilter.java`

```java
package com.am.marketdata.config;

import com.modernportfolio.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * Custom JWT filter for market-data application that validates JWT tokens
 * without requiring user management (UserDao/JwtService).
 */
@Component
public class MarketDataJwtRequestFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(MarketDataJwtRequestFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, 
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        final String requestTokenHeader = request.getHeader("Authorization");

        String username = null;
        String jwtToken = null;

        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);
            try {
                username = jwtUtil.getUsernameFromToken(jwtToken);
            } catch (IllegalArgumentException e) {
                logger.info("Illegal Argument while fetching the username: {}", e.getMessage());
            } catch (ExpiredJwtException e) {
                logger.info("Given jwt token is expired: {}", e.getMessage());
            } catch (MalformedJwtException e) {
                logger.info("Invalid Token: {}", e.getMessage());
            } catch (Exception e) {
                logger.error("Error validating JWT token", e);
            }
        } else {
            logger.debug("JWT token does not start with Bearer");
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                // Create a simple UserDetails object without querying the database
                UserDetails userDetails = User.builder()
                        .username(username)
                        .password("") // No password needed for token validation
                        .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
                        .build();

                // Validate the token
                if (jwtUtil.validateToken(jwtToken, userDetails)) {
                    UsernamePasswordAuthenticationToken authenticationToken = 
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                    logger.debug("Successfully authenticated user: {}", username);
                }
            } catch (Exception e) {
                logger.error("Error during token validation for user: {}", username, e);
            }
        }
        
        filterChain.doFilter(request, response);
    }
}
```

## Step 5: Create SecurityConfig in Your Project

Create this NEW file in your am-market-data project:
Location: `src/main/java/com/am/marketdata/config/SecurityConfig.java`

```java
package com.am.marketdata.config;

import com.modernportfolio.configuration.JwtAuthenticationEntryPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private MarketDataJwtRequestFilter marketDataJwtRequestFilter;

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(Customizer.withDefaults())
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/authenticate", "/registerNewUser").permitAll()
                .requestMatchers(HttpHeaders.ALLOW).permitAll()
                .requestMatchers("/actuator/**", "/health/**").permitAll()
                // Protect ALL your market data endpoints
                .requestMatchers("/api/v1/**").authenticated()
                .requestMatchers("/auth/login-url").authenticated()
                .anyRequest().authenticated())
            .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthenticationEntryPoint))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.addFilterBefore(marketDataJwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
```

## Step 6: Your Controllers Don't Need Changes!

Your EXISTING controllers will automatically be protected. The JWT validation happens in the filter layer.

If you want to access authenticated user info in your controller:

```java
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@GetMapping("/auth/login-url")
public ResponseEntity<Map<String, String>> getLoginUrl() {
    // Get authenticated user
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String username = auth.getName();
    
    // Your existing code...
}
```

## Step 7: Build and Run

```bash
# Build login-management-system first (this creates the jwt-auth-starter JAR)
cd D:\am\login-management-system
mvn clean install

# Then build and run your am-market-data
cd D:\am\am-market-data
mvn clean package
java -jar market-data-app/target/market-data-app-1.0-SNAPSHOT.jar
```

## Testing

1. **Get JWT Token from login-management-system:**
```bash
POST http://localhost:8080/authenticate
Content-Type: application/json

{
  "userName": "your-username",
  "userPassword": "your-password"
}
```

Response:
```json
{
  "jwtToken": "eyJhbGciOiJIUzI1NiJ9...",
  "user": {
    "userName": "your-username",
    ...
  }
}
```

2. **Use Token in am-market-data:**
```bash
GET http://localhost:8081/api/v1/market-data
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

## Summary

✅ **Add ONLY 1 dependency** - `jwt-auth-starter` (version 0.1.0-SNAPSHOT)  
✅ **Add ONLY 1 import** - `JwtValidationAutoConfiguration` in your main class  
✅ Add JWT secret to application.properties  
✅ Create custom `MarketDataJwtRequestFilter` (no database lookups)  
✅ Create `SecurityConfig.java` with your custom filter  
✅ Your existing controllers work as-is  

## What Gets Imported Automatically

When you use `@Import(JwtValidationAutoConfiguration.class)`, you automatically get:

✅ `JwtUtil` - For token parsing and validation  
✅ `JwtAuthenticationEntryPoint` - For handling authentication errors  

## What Gets Excluded Automatically

The auto-configuration automatically excludes these (they require database):

❌ `JwtService` - Requires UserDao  
❌ `UserService` - Requires UserDao and RoleDao  
❌ `RoleService` - Requires RoleDao  
❌ `JwtRequestFilter` - Requires JwtService (you create your own custom filter)  
❌ `WebSecurityConfiguration` - You provide your own SecurityConfig  

## Architecture

- **login-management-system** (port 8080): Issues JWT tokens, manages users
- **am-market-data** (port 8081): Validates JWT tokens only, no user management

**No multiple dependencies needed - just ONE JAR with ONE import!** 🚀
