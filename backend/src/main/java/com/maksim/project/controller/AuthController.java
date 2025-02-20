package com.maksim.project.controller;

import com.maksim.project.model.LoginRequest;
import com.maksim.project.model.LoginResponse;
import com.maksim.project.security.JwtUtil;
import com.maksim.project.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("/auth")
public class AuthController {


    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    public AuthController(AuthenticationManager authenticationManager, UserService userService, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest){
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
            System.out.println("Authentication successful for email: " + loginRequest.getEmail());
        } catch (Exception   e){
            e.printStackTrace();
            System.out.println("Authentication failed for email: " + loginRequest.getEmail());
            return ResponseEntity.status(401).build();
        }
        System.out.println("Generating token for email: " + loginRequest.getEmail());
        return ResponseEntity.ok(new LoginResponse(jwtUtil.generateToken(loginRequest.getEmail())));
    }

}
