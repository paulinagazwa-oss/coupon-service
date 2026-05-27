package com.github.paulinagazwa.oss.coupon_service;

import com.github.paulinagazwa.oss.coupon_service.config.AdvisoryLockConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication (scanBasePackages = "com.github.paulinagazwa.oss.coupon_service")
@Import(value = {AdvisoryLockConfig.class})
public class CouponServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CouponServiceApplication.class, args);
	}

}
