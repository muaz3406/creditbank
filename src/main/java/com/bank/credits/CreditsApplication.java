package com.bank.credits;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class CreditsApplication {

    public static void main(String[] args) {
        SpringApplication.run(CreditsApplication.class, args);
    }

}
