package com.BussinesCardApp.demo.user.authentication.Service;

import com.BussinesCardApp.demo.user.authentication.DTO.LoginRequest;
import com.BussinesCardApp.demo.user.authentication.DTO.LoginResponse;
import com.BussinesCardApp.demo.user.authentication.JWT.JWTUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    private final AuthenticationManager authManager;
    private final JWTUtil jwtUtil;

    public AuthenticationService(AuthenticationManager authManager,
                                 JWTUtil jwtUtil) {
        this.authManager = authManager;
        this.jwtUtil     = jwtUtil;
    }

    /**
     * Attempts to authenticate the given credentials.
     * On success, stores the Authentication in the SecurityContext
     * and returns a JWT wrapped in a LoginResponse.
     */
    public LoginResponse authenticate(LoginRequest request) {
        // 1. Perform authentication
        Authentication authentication = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        // 2. Store in SecurityContext
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 3. Generate JWT token on the injected bean
        String token = jwtUtil.generateToken(authentication);

        // 4. Return it in your response DTO
        return new LoginResponse(token);
    }
}
