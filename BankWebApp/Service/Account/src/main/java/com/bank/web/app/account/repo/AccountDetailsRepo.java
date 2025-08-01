package com.bank.web.app.account.repo;

import com.bank.web.app.account.model.AccountDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AccountDetailsRepo extends JpaRepository<AccountDetails,Long> {
    boolean existsByAccountNumber(Long acNum);

    @Query("SELECT a FROM AccountDetails a WHERE a.branch.branchId = :id AND a.isActive = false")
    List<AccountDetails> findAccountByBranchId(@Param("id") Long id);

   AccountDetails findByAccountNumber(Long num);
}
