//package com.dep.soms.dto.auth;
//
//import lombok.Data;
//import lombok.Getter;
//import lombok.Setter;
//
//import java.util.List;
//
//@Getter
//@Setter
//public class JwtResponse {
//    private String token;
//    private String type = "Bearer";
//    private Long id;
//    private String username;
//    private String email;
//    private List<String> roles;
//
//    public JwtResponse(String accessToken, Long id, String username, String email, List<String> roles) {
//        this.token = accessToken;
//        this.id = id;
//        this.username = username;
//        this.email = email;
//        this.roles = roles;
//    }
//}
package com.dep.soms.dto.auth;

import java.util.List;

public class JwtResponse {
    private String token;  // Changed from accessToken to token for consistency
    private String type = "Bearer";
    private Long id;
    private String username;
    private String email;
    private List<String> roles;

    public JwtResponse(String accessToken, Long id, String username, String email, List<String> roles) {
        this.token = accessToken;  // Changed from this.accessToken = accessToken
        this.id = id;
        this.username = username;
        this.email = email;
        this.roles = roles;
    }

    // Renamed from getAccessToken to getToken for consistency
    public String getToken() {
        return token;
    }

    // Renamed from setAccessToken to setToken for consistency
    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<String> getRoles() {
        return roles;
    }
}
