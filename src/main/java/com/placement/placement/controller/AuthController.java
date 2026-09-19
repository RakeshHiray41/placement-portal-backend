package com.placement.placement.controller;

import com.placement.placement.dto.AuthRequest;

import com.placement.placement.dto.LoginRequestDto;

import com.placement.placement.entity.Admin;
import com.placement.placement.repository.AdminRepository;
import com.placement.placement.security.JwtUtil;
import com.placement.placement.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final AdminRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private  final AdminService adminService;

    @PostMapping("/login")
    public ResponseEntity<AuthRequest>login(@RequestBody LoginRequestDto loginRequestDto)
    {
        return ResponseEntity.ok(adminService.login(loginRequestDto));
    }

    // REGISTER (only for development)
//    @PostMapping("/register")
//    public ResponseEntity<?> register(@RequestBody AuthRequest request) {
//
//        if (userRepo.findByUsername(request.getUsername()).isPresent()) {
//            return ResponseEntity.badRequest().body("Username already exists.");
//        }
//
//        Admin user = new Admin();
//        user.setUsername(request.getUsername());
//        user.setPassword(passwordEncoder.encode(request.getPassword()));
//
//        userRepo.save(user);
//
//        return ResponseEntity.ok("User registered successfully");
//    }


}
