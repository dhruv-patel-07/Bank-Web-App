package com.bank.web.app.transaction.Repo;


import com.bank.web.app.transaction.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AccountRepo extends JpaRepository<Account,Long> {

    @Query("SELECT a FROM Account a WHERE a.uid = :uid AND a.accountNum = :ac")
    Account findByUidAndAccountNum(@Param("uid") String uid, @Param("ac") Long ac);

    Boolean existsByAccountNum(Long num);

    Account findByAccountNum(Long num);

}
