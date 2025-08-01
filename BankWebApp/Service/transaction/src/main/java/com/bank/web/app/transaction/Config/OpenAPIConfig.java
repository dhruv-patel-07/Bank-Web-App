package com.bank.web.app.transaction.Config;


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI customOpenAPI() {
//        Server customServer = new Server();
//        customServer.setUrl("http://localhost:8222"); //
        return new OpenAPI()
                .servers(List.of(new Server().url("http://localhost:8222")))
                .info(new Info()
                        .title("Transaction Service API")
                        .version("1.0")
                        .description("The Transaction Service is a core microservice in a banking ecosystem responsible for make Transaction for Deposit Money,Transfer Money,Loan Payment,Recurring Payment"));

    }
}