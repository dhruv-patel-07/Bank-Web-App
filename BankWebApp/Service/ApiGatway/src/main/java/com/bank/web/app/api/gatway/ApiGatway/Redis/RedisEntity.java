package com.bank.web.app.api.gatway.ApiGatway.Redis;


import lombok.Data;

import java.io.Serializable;

@Data
public class RedisEntity implements Serializable {

    String access_token;
    String refresh_token;
}
