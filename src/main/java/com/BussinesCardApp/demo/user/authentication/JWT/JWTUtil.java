package com.BussinesCardApp.demo.user.authentication.JWT;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.JwtException;
import java.util.Date;

@Component
public class JWTUtil {

    /**
     * Secret key for signing JWTs.
     * Define this in your application.properties:
     *   app.jwtSecret=YourSuperSecretKey
     */
    @Value("${app.jwtSecret}")
    private String jwtSecret;

    /**
     * Expiration time in milliseconds.
     * Define this in application.properties:
     *   app.jwtExpirationMs=86400000   # e.g. 24h
     */
    @Value("${app.jwtExpirationMs}")
    private int jwtExpirationMs;

    /**
     * Generate a signed JWT containing the username as subject,
     * and issued/expiration dates.
     */
    public String generateToken(Authentication authentication) {
        String username = authentication.getName();
        Date now       = new Date();
        Date expiry    = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(SignatureAlgorithm.HS256, jwtSecret)
                .compact();
    }

    /**
     * (Optional) parse the token and return the username
     */
    public String getUsernameFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    /**
     * (Optional) validate the token signature and expiration
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token);
            return true;
        } catch (JwtException ex) {
            // e.g. ExpiredJwtException, MalformedJwtException, etc.
            // You may want to log the exception here
            return false;
        }
    }
}