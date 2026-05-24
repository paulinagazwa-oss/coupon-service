package com.github.paulinagazwa.oss.coupon_service.service.impl;

import com.github.paulinagazwa.oss.coupon_service.api.model.CouponResponse;
import com.github.paulinagazwa.oss.coupon_service.api.model.CreateCouponRequest;
import com.github.paulinagazwa.oss.coupon_service.api.model.DiscountType;
import com.github.paulinagazwa.oss.coupon_service.api.model.RedeemCouponRequest;
import com.github.paulinagazwa.oss.coupon_service.api.model.RedeemCouponResponse;
import com.github.paulinagazwa.oss.coupon_service.entity.CouponEntity;
import com.github.paulinagazwa.oss.coupon_service.exception.CouponAlreadyExistsException;
import com.github.paulinagazwa.oss.coupon_service.exception.CouponNotFoundException;
import com.github.paulinagazwa.oss.coupon_service.exception.InvalidDiscountException;
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

		// Check if coupon with the same code already exists
		ensureUniqueName(createCouponRequest.getName());

		// Check if discount is valid (not negative, not greater than 100% for PERCENTAGE type)
		ensureValidDiscount(createCouponRequest.getDiscount(), createCouponRequest.getDiscountType());

		CouponEntity couponEntity = couponMapper.toEntity(createCouponRequest);
		couponEntity.setCreatedAt(OffsetDateTime.now());
		couponEntity.setCurrentRedemptions(0);

		// Generate unique code if not provided in the request
		generateNameIfAbsent(couponEntity);

		couponEntity = couponRepository.save(couponEntity);

		return couponMapper.toModel(couponEntity);

	}

	private void ensureUniqueName(String name) {

		if (name != null && couponRepository.existsByName(name)) {
			throw new CouponAlreadyExistsException(name);
		}
	}

	private void ensureValidDiscount(Float discount, DiscountType discountType) {

		if (discount < 0) {
			throw new InvalidDiscountException(discount);
		}
		if (DiscountType.PERCENTAGE.equals(discountType) && discount > 100) {
			throw new InvalidDiscountException(discount);
		}
	}

	private void generateNameIfAbsent(CouponEntity couponEntity) {

		if (couponEntity.getName() == null || couponEntity.getName().isBlank()) {
			couponEntity.setName(UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase());
		}
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
