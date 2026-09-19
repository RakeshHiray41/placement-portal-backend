package com.placement.placement.service;

import com.placement.placement.dto.AuthRequest;
import com.placement.placement.dto.LoginRequestDto;
import com.placement.placement.entity.Admin;
import com.placement.placement.repository.AdminRepository;
import com.placement.placement.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService
{

private final AuthenticationManager authenticationManager;
private  final JwtUtil jwtUtil;

    public AuthRequest login(LoginRequestDto loginRequestDto)
    {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequestDto .getUsername(),loginRequestDto.getPassword())
        );

        Admin  admin = (Admin) authentication.getPrincipal();

        String tokan = jwtUtil.generateAccessToken(admin);
        return new AuthRequest(tokan,admin.getId());
    }



























//    private final AdminRepository adminRepo;
//    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
//
//    public Admin validateAdmin(String username, String password) {
//        return adminRepo.findByUsername(username)
//                .filter(admin -> encoder.matches(password, admin.getPassword()))
//                .orElse(null);
//    }
//
//    public Admin saveAdmin(String username, String password) {
//        Admin admin = new Admin();
//        admin.setUsername(username);
//        admin.setPassword(encoder.encode(password));
//        return adminRepo.save(admin);
//    }


}
