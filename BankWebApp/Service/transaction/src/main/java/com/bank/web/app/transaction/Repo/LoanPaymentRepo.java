package com.bank.web.app.transaction.Repo;

import com.bank.web.app.transaction.model.LoanPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Map;

public interface LoanPaymentRepo extends JpaRepository<LoanPayment,Long> {

    List<LoanPayment> findAllByLpIdIn(List<Long> lpIds);

//    @Query("SELECT Account.balance,LoanPayment.lpId FROM LoanPayment LEFT JOIN Account ON LoanPayment.accountNum")

}
