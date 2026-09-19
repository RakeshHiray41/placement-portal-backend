//package com.placement.placement.config;
//
//import com.placement.placement.security.JwtUtil;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.stereotype.Service;
//
//import java.util.Collection;
//
//@Service
//public class JwtService {
//
//    private final JwtUtil jwtUtil;
//
//    @Autowired
//    public JwtService(JwtUtil jwtUtil) {
//        this.jwtUtil = jwtUtil;
//    }
//
//    public String extractUsername(String token) {
//        return jwtUtil.extractUsername(token);
//    }
//
//    public boolean isTokenValid(String token) {
//        return jwtUtil.isTokenValid(token);
//    }
//
//    public String generateToken(String username) {
//        return jwtUtil.generateToken(username);
//    }
//
//    public Collection<? extends GrantedAuthority> getAuthorities(String username) {
//        return null;
//    }
//}
