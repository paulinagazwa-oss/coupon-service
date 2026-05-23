package com.github.paulinagazwa.oss.coupon_service.exception;

public class CouponAlreadyExistsException extends RuntimeException {

    public CouponAlreadyExistsException(String name) {
        super("Coupon already exists: " + name);
    }
}

