package com.helljh.publicfacility;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PublicfacilityApplication {

	public static void main(String[] args) {
		SpringApplication.run(PublicfacilityApplication.class, args);
	}

}
