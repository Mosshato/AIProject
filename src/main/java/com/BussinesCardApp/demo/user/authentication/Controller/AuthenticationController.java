package com.BussinesCardApp.demo.user.authentication.Controller;


import com.BussinesCardApp.demo.user.authentication.DTO.LoginRequest;
import com.BussinesCardApp.demo.user.authentication.DTO.LoginResponse;
import com.BussinesCardApp.demo.user.authentication.Service.AuthenticationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/login")
public class AuthenticationController {

    private final AuthenticationService authService;

    public AuthenticationController(AuthenticationService authService) {
        this.authService = authService;
    }

    @PostMapping
    public ResponseEntity<LoginResponse> login(
            @RequestBody LoginRequest request
    ) {
        LoginResponse resp = authService.authenticate(request);
        return ResponseEntity.ok(resp);
    }
}
