package com.github.paulinagazwa.oss.coupon_service.controller;

import com.github.paulinagazwa.oss.coupon_service.api.CouponApi;
import com.github.paulinagazwa.oss.coupon_service.api.model.CouponResponse;
import com.github.paulinagazwa.oss.coupon_service.api.model.CreateCouponRequest;
import com.github.paulinagazwa.oss.coupon_service.api.model.RedeemCouponRequest;
import com.github.paulinagazwa.oss.coupon_service.api.model.RedeemCouponResponse;
import com.github.paulinagazwa.oss.coupon_service.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class CouponController implements CouponApi {

	private final CouponService couponService;

    @Override
    public ResponseEntity<CouponResponse> registerCoupon(CreateCouponRequest createCouponRequest) {

		return ResponseEntity.ok(couponService.createCoupon(createCouponRequest));
    }

    @Override
    public ResponseEntity<CouponResponse> getCouponById(@PathVariable UUID couponId) {

        return ResponseEntity.ok(couponService.getCouponById(couponId));
    }

    @Override
    public ResponseEntity<RedeemCouponResponse> redeemCoupon(@PathVariable UUID couponId, RedeemCouponRequest redeemCouponRequest) {

		//TODO: Get client IP address from request context and pass it to the service layer for geolocation checks
		return ResponseEntity.status(HttpStatus.CREATED).body(couponService.redeemCoupon(couponId, redeemCouponRequest, ""));
    }
}

