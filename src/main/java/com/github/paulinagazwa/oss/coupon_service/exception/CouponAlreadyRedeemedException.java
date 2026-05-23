package com.github.paulinagazwa.oss.coupon_service.exception;

import java.util.UUID;

public class CouponAlreadyRedeemedException extends RuntimeException {

    public CouponAlreadyRedeemedException(UUID couponId) {
        super("Coupon already redeemed: " + couponId);
    }
}

