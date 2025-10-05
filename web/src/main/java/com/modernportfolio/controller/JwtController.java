package com.modernportfolio.controller;

import com.modernportfolio.model.JwtRequest;
import com.modernportfolio.model.JwtResponse;
import com.modernportfolio.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class JwtController {

    @Autowired
    private JwtService jwtService;

    @PostMapping({"/authenticate"})
    public JwtResponse createJwtToken(@RequestBody JwtRequest jwtRequest) throws Exception {
        return jwtService.createJwtToken(jwtRequest);
    }

    @GetMapping({"/getHelloworld"})
    public String createJwtToken() throws Exception {
        return "Hello Suman weds Priyanka! :)";
    }
}
