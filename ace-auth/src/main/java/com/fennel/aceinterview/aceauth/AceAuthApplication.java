package com.fennel.aceinterview.aceauth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@RefreshScope
@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = {"com.fennel.aceinterview"})
public class AceAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(AceAuthApplication.class, args);
    }

}
