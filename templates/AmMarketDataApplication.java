package com.am.marketdata;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Main Application Class for am-market-data
 * Copy this template and modify the package name as needed
 *
 * The @ComponentScan annotation includes com.modernportfolio to enable
 * automatic discovery of JWT authentication components from login-management-system
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.am.marketdata", "com.modernportfolio"})
public class AmMarketDataApplication {

    public static void main(String[] args) {
        SpringApplication.run(AmMarketDataApplication.class, args);
    }
}
