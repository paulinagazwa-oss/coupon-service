package com.github.paulinagazwa.oss.coupon_service.service.impl;

import com.github.paulinagazwa.oss.coupon_service.api.model.CouponResponse;
import com.github.paulinagazwa.oss.coupon_service.api.model.CreateCouponRequest;
import com.github.paulinagazwa.oss.coupon_service.api.model.RedeemCouponRequest;
import com.github.paulinagazwa.oss.coupon_service.api.model.RedeemCouponResponse;
import com.github.paulinagazwa.oss.coupon_service.entity.CouponEntity;
import com.github.paulinagazwa.oss.coupon_service.exception.CouponNotFoundException;
import com.github.paulinagazwa.oss.coupon_service.mapper.CouponMapper;
import com.github.paulinagazwa.oss.coupon_service.repository.CouponRepository;
import com.github.paulinagazwa.oss.coupon_service.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

	private final CouponRepository couponRepository;

	private final CouponMapper couponMapper;

	@Override
	public CouponResponse createCoupon(CreateCouponRequest createCouponRequest) {

		// TODO check if coupon with the same code already exists
		// TODO check if discount is valid (e.g. not negative, not greater than 100%)
		CouponEntity couponEntity = couponMapper.toEntity(createCouponRequest);
		couponEntity.setCreatedAt(OffsetDateTime.now());
		couponEntity.setCurrentRedemptions(0);
		// TODO generate unique code for the coupon if not provided in the request

		couponEntity = couponRepository.save(couponEntity);

		return couponMapper.toModel(couponEntity);

	}

	@Override
	public RedeemCouponResponse redeemCoupon(RedeemCouponRequest redeemCouponRequest) {

		// TODO check if coupon exists
		// TODO check if coupon is valid for the given country (use free ip geolocation service to get the country from the request)
		// TODO check if coupon is valid (e.g. not expired, not redeemed more than maxRedemptions)
		// TODO increment currentRedemptions and save the coupon
		return null;
	}

	@Override
	public @Nullable CouponResponse getCouponById(UUID couponId) {

		return couponRepository.findById(couponId)
				.map(couponMapper::toModel)
				.orElseThrow(() -> new CouponNotFoundException(couponId));
	}

}
