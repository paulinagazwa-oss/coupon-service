package com.github.paulinagazwa.oss.coupon_service.exception;

import java.util.UUID;

public class CouponNotFoundException extends RuntimeException {

    public CouponNotFoundException(UUID couponId) {
        super("Coupon not found: " + couponId);
    }
}

