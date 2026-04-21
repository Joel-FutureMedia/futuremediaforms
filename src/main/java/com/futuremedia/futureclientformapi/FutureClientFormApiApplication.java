package com.futuremedia.futureclientformapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class FutureClientFormApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(FutureClientFormApiApplication.class, args);
    }
}