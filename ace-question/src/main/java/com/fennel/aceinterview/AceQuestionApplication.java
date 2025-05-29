package com.fennel.aceinterview;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.fennel.aceinterview.question.dao")
@SpringBootApplication
public class AceQuestionApplication {

	public static void main(String[] args) {
		SpringApplication.run(AceQuestionApplication.class, args);
	}

}
