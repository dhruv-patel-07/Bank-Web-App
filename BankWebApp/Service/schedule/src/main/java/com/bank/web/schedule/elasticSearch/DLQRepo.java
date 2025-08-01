package com.bank.web.schedule.elasticSearch;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface DLQRepo extends ElasticsearchRepository<DLQ,String> {

}
