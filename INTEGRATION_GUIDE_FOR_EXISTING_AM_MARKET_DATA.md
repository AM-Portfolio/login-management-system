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
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

That's it! This single JAR includes:
- All JWT utilities (JwtUtil)
- All security filters (JwtRequestFilter, JwtAuthenticationEntryPoint)
- All authentication services (JwtService)
- All entity models (User, Role)
- All repositories
- All required dependencies (jjwt, spring-security, etc.)

## Step 2: Add JWT Configuration to Your application.properties

Add this to your EXISTING application.properties file:

```properties
# JWT Configuration (MUST match login-management-system)
jwt.secret=g4ASf2sEJk08Y3GoiHKdF0F78E2Vj34S+KmN1IJdF2ncxEh0bxyT1XQEPYTc7SQvqO+3wd9MC8X7S6nG0Rb0TQ==

# Allow circular references for login-management-system integration
spring.main.allow-circular-references=true

# JWT Logging
logging.level.com.modernportfolio=DEBUG
```

## Step 3: Update Your Main Application Class

Update your EXISTING AmMarketDataApplication.java (or whatever it's called):

```java
package com.am.marketdata;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
    "com.am.marketdata",           // Your existing package
    "com.modernportfolio"          // Scan login-management-system components
})
public class AmMarketDataApplication {
    public static void main(String[] args) {
        SpringApplication.run(AmMarketDataApplication.class, args);
    }
}
```

## Step 4: Create SecurityConfig in Your Project

Create this NEW file in your am-market-data project:
Location: `src/main/java/com/am/marketdata/config/SecurityConfig.java`

```java
package com.am.marketdata.config;

import com.modernportfolio.configuration.JwtRequestFilter;
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
    private JwtRequestFilter jwtRequestFilter;

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

        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
```

## Step 5: Your Controllers Don't Need Changes!

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

## Step 6: Build and Run

```bash
# Build login-management-system first (this creates the jwt-auth-starter JAR)
cd D:\am\login-management-system
mvn clean install

# Then build and run your am-market-data
cd D:\am\am-market-data
mvn clean install
mvn spring-boot:run
```

## Testing

1. **Get JWT Token:**
```bash
POST http://localhost:8080/authenticate
Content-Type: application/json

{
  "userName": "your-username",
  "userPassword": "your-password"
}
```

2. **Use Token:**
```bash
GET http://localhost:8080/auth/login-url
Authorization: Bearer <your-jwt-token>
```

## Summary

✅ **Add ONLY 1 dependency** - `jwt-auth-starter` (includes everything)  
✅ Add JWT secret to application.properties  
✅ Update main class with @ComponentScan  
✅ Create SecurityConfig.java  
✅ Your existing controllers work as-is  

**No multiple dependencies needed - just ONE JAR!**
