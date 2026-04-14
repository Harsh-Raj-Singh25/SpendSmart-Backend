package com.spendsmart.recurring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableFeignClients
@EnableScheduling // to make the cron job (@Scheduled) actually work
public class RecurringTransactionServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(RecurringTransactionServiceApplication.class, args);
	}

}
