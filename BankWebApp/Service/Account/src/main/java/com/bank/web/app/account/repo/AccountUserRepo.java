package com.bank.web.app.account.repo;

import com.bank.web.app.account.model.AccountUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountUserRepo extends JpaRepository<AccountUser,Integer> {

    boolean existsByUid(String uid);
    AccountUser findByUid(String uid);

}
