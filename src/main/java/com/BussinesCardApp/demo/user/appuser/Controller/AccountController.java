// src/main/java/com/BussinesCardApp/demo/user/controller/AccountController.java
package com.BussinesCardApp.demo.user.controller;

import com.BussinesCardApp.demo.user.appuser.AppUser;
import com.BussinesCardApp.demo.user.appuser.UserAccountDTO;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AccountController {

    /**
     * GET /myaccount
     * Returns basic profile info for the currently authenticated user.
     */
    @GetMapping("/myaccount")
    public UserAccountDTO myAccount(@AuthenticationPrincipal AppUser user) {
        // SecurityConfig should already require authentication for /myaccount,
        // so 'user' is never null. If you want an extra guard:
        if (user == null) {
            throw new RuntimeException("Unauthorized");   // or use ResponseStatusException
        }

        return new UserAccountDTO(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName()
        );
    }
}
