package com.fennel.aceinterview;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients(basePackages = "com.fennel.aceinterview.question.feign")
@EnableDiscoveryClient
@MapperScan("com.fennel.aceinterview.question.dao")
@SpringBootApplication
public class AceQuestionApplication {

	public static void main(String[] args) {
		SpringApplication.run(AceQuestionApplication.class, args);
	}

}
