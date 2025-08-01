package com.bank.web.app.account.repo;

import com.bank.web.app.account.model.Branch;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BranchRepo extends JpaRepository<Branch,Long> {

    Branch findByBranchCode(String code);

    Branch findByBranchName(String branchName);
}
