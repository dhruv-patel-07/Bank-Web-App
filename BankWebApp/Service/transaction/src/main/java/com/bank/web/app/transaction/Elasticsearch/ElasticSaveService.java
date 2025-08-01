package com.bank.web.app.transaction.Elasticsearch;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class ElasticSaveService {

    @Autowired
    private UserRepo repo;

    @Async
    public void SaveToElastic(UserDetails userDetails){
        repo.save(userDetails);

    }
}
