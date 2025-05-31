package com.BussinesCardApp.demo.user.authentication.DTO;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String token;
    /**
     * "Bearer" tells clients how to format the HTTP
     * Authorization header on subsequent requests:
     *
     *   Authorization: Bearer <token>
     */
    private String type = "Bearer";

    public LoginResponse(String token) {
        this.token = token;
    }
}
