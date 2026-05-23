package com.github.paulinagazwa.oss.coupon_service.service;

import com.github.paulinagazwa.oss.coupon_service.api.model.CouponResponse;
import com.github.paulinagazwa.oss.coupon_service.api.model.CreateCouponRequest;
import com.github.paulinagazwa.oss.coupon_service.api.model.RedeemCouponRequest;
import com.github.paulinagazwa.oss.coupon_service.api.model.RedeemCouponResponse;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

public interface CouponService {

	CouponResponse createCoupon(CreateCouponRequest createCouponRequest);

	RedeemCouponResponse redeemCoupon(RedeemCouponRequest redeemCouponRequest);

	@Nullable CouponResponse getCouponById(UUID couponId);
}
