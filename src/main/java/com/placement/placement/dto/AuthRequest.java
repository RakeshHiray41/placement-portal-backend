package com.placement.placement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AuthRequest
{

    String jwt;
    long id;

}










//    private String username;
//    private String password;
//
//    public String getJwt() {
//        return jwt;
//    }
//
//    public void setJwt(String jwt) {
//        this.jwt = jwt;
//    }
//
//    private String jwt;
//
//
//    public String getUsername() { return username; }
//    public void setUsername(String username) { this.username = username; }
//
//    public String getPassword() { return password; }
//    public void setPassword(String password) { this.password = password; }
//}
