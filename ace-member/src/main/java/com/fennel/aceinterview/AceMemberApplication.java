package com.fennel.aceinterview;

import com.fennel.common.config.WebMvcConfig;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;

@EnableFeignClients(basePackages = "com.fennel.aceinterview.member.feign")
@EnableDiscoveryClient
@MapperScan("com.fennel.aceinterview.member.dao")
@SpringBootApplication
@Import({WebMvcConfig.class})
public class AceMemberApplication {

	public static void main(String[] args) {
		SpringApplication.run(AceMemberApplication.class, args);
	}

}
