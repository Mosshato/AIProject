package com.BussinesCardApp.demo.user.authentication;

import com.BussinesCardApp.demo.user.appuser.AppUser;
import com.BussinesCardApp.demo.user.appuser.AppUserRepository;
import com.BussinesCardApp.demo.user.authentication.jwt.JwtGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthenticationService {

    private final AppUserRepository userRepo;
    private final PasswordEncoder   encoder;
    private final JwtGenerator      jwtGen;

    public AuthenticationService(AppUserRepository userRepo,
                                 PasswordEncoder encoder,
                                 JwtGenerator jwtGen) {
        this.userRepo = userRepo;
        this.encoder  = encoder;
        this.jwtGen   = jwtGen;
    }

    @Transactional(readOnly = true)
    public String login(AuthenticationDTO dto) {

        AppUser user = userRepo.findByEmail(dto.email())
                .orElseThrow(() ->
                        new BadCredentialsException("Email sau parolă greșită"));  // mesaj generic

        /* 1️⃣  e-mail confirmat?  */
        if (!user.isEnabled()) {
            throw new DisabledException(
                    "Contul nu este activat. Verifică e-mailul și apasă pe linkul de confirmare.");
        }

        /* 2️⃣  cont blocat de admin / bruteforce  */
        if (!user.isAccountNonLocked()) {
            throw new LockedException("Contul este blocat. Contactează administratorul.");
        }

        /* 3️⃣  parolă corectă?  */
        if (!encoder.matches(dto.password(), user.getPassword())) {
            throw new BadCredentialsException("Email sau parolă greșită");
        }

        return jwtGen.generateToken(
                user.getId(),
                user.getAppUserRole().name(),
                user.getEmail()
        );
    }
}

