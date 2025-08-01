package com.bank.web.app.auth.repo;

import com.bank.web.app.auth.model.UserVerification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VerificationRepo extends JpaRepository<UserVerification,String> {
    boolean existsByEmail(String email);
}
