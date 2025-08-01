package com.bank.web.app.account.repo;

import com.bank.web.app.account.model.RecurringAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecurringAccountRepo extends JpaRepository<RecurringAccount,Long> {

    RecurringAccount findByAccountNum(Long acNum);
}
