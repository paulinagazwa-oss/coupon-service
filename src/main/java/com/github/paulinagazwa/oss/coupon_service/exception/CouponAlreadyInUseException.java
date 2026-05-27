package com.github.paulinagazwa.oss.coupon_service.exception;

public class CouponAlreadyInUseException extends RuntimeException{

	public CouponAlreadyInUseException() {
		super("Coupon is busy at the moment, try again later.");
	}
}
