package com.fennel.aceinterview;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@RefreshScope
@EnableDiscoveryClient
@SpringBootApplication
public class AceGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(AceGatewayApplication.class, args);
    }

}
