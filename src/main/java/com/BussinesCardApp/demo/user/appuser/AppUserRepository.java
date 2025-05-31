package com.BussinesCardApp.demo.user.appuser;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository                      // optional when extending Spring Data interfaces
@Transactional(readOnly = true)  // now resolves to Spring's annotation
public interface AppUserRepository
        extends MongoRepository<AppUser, String> {

    Optional<AppUser> findByEmail(String email);

    /**
     * Enable the user with the given email.
     * Returns 1 if found & updated, 0 otherwise.
     */
    @Transactional                   // write transaction
    default int enableAppUser(String email) {
        return findByEmail(email)
                .map(user -> {
                    user.setEnabled(true);
                    save(user);
                    return 1;
                })
                .orElse(0);
    }
}