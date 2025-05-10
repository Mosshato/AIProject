package com.BussinesCardApp.demo.USER.registration.token;

import com.BussinesCardApp.demo.USER.appuser.AppUser;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Document(collection = "confirmationTokens")
public class ConfirmationToken {

    @Id
    private String id;              // Mongo will auto‑generate an ObjectId here

    private String token;

    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private LocalDateTime confirmedAt;

    /**
     * If you want a DBRef to your AppUser document so Spring Data
     * will fetch it lazily/automatically:
     */
    @DBRef
    private AppUser appUser;

    public ConfirmationToken(String token,
                             LocalDateTime createdAt,
                             LocalDateTime expiresAt,
                             AppUser appUser) {
        this.token     = token;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.appUser   = appUser;
    }
    public LocalDateTime getConfirmedAt() {
        return confirmedAt;
    }
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    public AppUser getAppUser() {
        return appUser;
    }

    public void setConfirmedAt(LocalDateTime confirmedAt) {
        this.confirmedAt = confirmedAt;
    }
}
