package com.booking.bookingsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableCaching
@EnableJpaAuditing
@EnableAsync
@EnableScheduling
@EnableTransactionManagement
public class BookingSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookingSystemApplication.class, args);
    }
}
