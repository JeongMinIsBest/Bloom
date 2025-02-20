package com.rootimpact.bloom;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {"com.rootimpact"})
@EntityScan("com.rootimpact")
@EnableJpaRepositories("com.rootimpact")
@EnableJpaAuditing
public class BloomApplication {

	public static void main(String[] args) {
		SpringApplication.run(BloomApplication.class, args);
	}

}
