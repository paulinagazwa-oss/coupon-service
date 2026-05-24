package com.github.paulinagazwa.oss.coupon_service.exception;

public class InvalidDiscountException extends RuntimeException {

    public InvalidDiscountException(Float discount) {
        super("Invalid discount value: " + discount + ". Discount must be non-negative and percentage discount cannot exceed 100%.");
    }
}
