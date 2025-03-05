package org.demo.bankingtestapi;

import org.demo.bankingtestapi.config.SecurityProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(SecurityProperties.class) // Enable configuration properties
public class BankingTestApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(BankingTestApiApplication.class, args);
	}

}
