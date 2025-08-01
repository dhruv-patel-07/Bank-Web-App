package com.bank.web.app.transaction.Repo;

import com.bank.web.app.transaction.model.Transaction;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface TransactionRepo extends JpaRepository<Transaction,Long> {

//    List<Transaction> findByAccount_AccountNumAndTimeStampBetween(Long accountNum, LocalDateTime start, LocalDateTime end);

    List<Transaction> findByAccount_AccountNumAndTimeStampBetween(Long accountNumber, LocalDateTime start, LocalDateTime end, Sort sort);
    List<Transaction> findByTimeStampBetween(LocalDateTime start, LocalDateTime end, Sort sort);


    List<Transaction> findByAccount_BranchNameAndTimeStampBetween(
            String branchName,
            LocalDateTime start,
            LocalDateTime end,
            Sort sort
    );

}
