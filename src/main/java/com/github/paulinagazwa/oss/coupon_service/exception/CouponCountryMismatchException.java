package com.github.paulinagazwa.oss.coupon_service.exception;

public class CouponCountryMismatchException extends RuntimeException {

	public CouponCountryMismatchException(String couponCountry, String clientCountry) {
		super("Coupon country mismatch: coupon is for " + couponCountry + ", but client is from " + clientCountry);
	}

}
