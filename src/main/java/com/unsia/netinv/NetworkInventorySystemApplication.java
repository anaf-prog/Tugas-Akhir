package com.unsia.netinv;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class NetworkInventorySystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(NetworkInventorySystemApplication.class, args);
	}

}
