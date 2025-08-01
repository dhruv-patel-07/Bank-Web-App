package com.bank.web.app.account.config;

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
                        .title("Account Service API")
                        .version("1.0")
                        .description("The Account Service is a core microservice in a banking ecosystem responsible for customer account management and financial operations like Fixed Deposits (FDs), Recurring Deposits, and Loan handling."));

    }
}