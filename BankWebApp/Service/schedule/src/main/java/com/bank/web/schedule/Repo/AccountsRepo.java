package com.bank.web.schedule.Repo;

import com.bank.web.schedule.model.Accounts;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AccountsRepo extends MongoRepository<Accounts,String> {

}
