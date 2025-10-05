package com.am.marketdata.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.HashMap;
import java.util.Map;

/**
 * Sample controller for am-market-data showing JWT-protected endpoints
 * Copy this template and modify as needed for your market data APIs
 */
@RestController
@RequestMapping("/api/market-data")
public class MarketDataController {

    @GetMapping("/stocks")
    public ResponseEntity<Map<String, Object>> getStocks() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Stock data retrieved successfully");
        response.put("user", username);
        response.put("data", "Sample stock data - this endpoint is JWT protected");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/bonds")
    public ResponseEntity<Map<String, Object>> getBonds() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Bond data retrieved successfully");
        response.put("user", username);
        response.put("data", "Sample bond data - this endpoint is JWT protected");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/portfolio")
    public ResponseEntity<Map<String, Object>> getPortfolio() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Portfolio data retrieved successfully");
        response.put("user", username);
        response.put("data", "Sample portfolio data - this endpoint is JWT protected");

        return ResponseEntity.ok(response);
    }
}
