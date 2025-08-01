package com.bank.web.app.transaction.Repo;

import com.bank.web.app.transaction.model.RecurringAccountPayment;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecurringAccountPaymentRepo extends JpaRepository<RecurringAccountPayment,Long> {

    List<RecurringAccountPayment> findByAccountNum(Long accountNum, Sort sort);

}

