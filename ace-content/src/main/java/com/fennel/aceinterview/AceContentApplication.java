package com.fennel.aceinterview;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@MapperScan("com.fennel.aceinterview.content.dao")
@SpringBootApplication
public class AceContentApplication {

	public static void main(String[] args) {
		SpringApplication.run(AceContentApplication.class, args);
	}

}
