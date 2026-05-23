package com.github.paulinagazwa.oss.coupon_service.controller;

import com.github.paulinagazwa.oss.coupon_service.api.CouponApi;
import com.github.paulinagazwa.oss.coupon_service.api.model.CouponResponse;
import com.github.paulinagazwa.oss.coupon_service.api.model.CreateCouponRequest;
import com.github.paulinagazwa.oss.coupon_service.api.model.RedeemCouponRequest;
import com.github.paulinagazwa.oss.coupon_service.api.model.RedeemCouponResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class CouponController implements CouponApi {

    @Override
    public ResponseEntity<CouponResponse> registerCoupon(CreateCouponRequest createCouponRequest) {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public ResponseEntity<CouponResponse> getCouponById(UUID couponId) {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public ResponseEntity<RedeemCouponResponse> redeemCoupon(UUID couponId, RedeemCouponRequest redeemCouponRequest) {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }
}

