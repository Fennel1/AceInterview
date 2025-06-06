package com.fennel.aceinterview;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@EnableDiscoveryClient
@MapperScan("com.fennel.aceinterview.study.dao")
@SpringBootApplication
public class AceStudyApplication {

    public static void main(String[] args) {
        SpringApplication.run(AceStudyApplication.class, args);
    }

}
