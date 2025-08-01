package com.bank.web.app.account.repo;

import com.bank.web.app.account.model.Loan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanRepo extends JpaRepository<Loan,Long> {
}
