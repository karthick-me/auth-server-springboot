package com.secureauth.authserver.auth.repository;

import com.secureauth.authserver.auth.model.EmailVerificationToken;
import com.secureauth.authserver.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailVerificationTokenRepository
        extends JpaRepository<EmailVerificationToken, Long> {

    EmailVerificationToken findByUser(User user);

    void deleteByUser(User user);
}
