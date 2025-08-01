package com.bank.web.app.account.repo;

import com.bank.web.app.account.model.FD;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FdRepo extends JpaRepository<FD,Long> {
}
