package com.bank.web.app.transaction.Elasticsearch;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface UserRepo extends ElasticsearchRepository<UserDetails,String> {
}
