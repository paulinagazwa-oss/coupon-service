package com.github.paulinagazwa.oss.coupon_service.service.impl;

import com.github.paulinagazwa.oss.coupon_service.api.model.CouponResponse;
import com.github.paulinagazwa.oss.coupon_service.api.model.CreateCouponRequest;
import com.github.paulinagazwa.oss.coupon_service.api.model.DiscountType;
import com.github.paulinagazwa.oss.coupon_service.api.model.RedeemCouponRequest;
import com.github.paulinagazwa.oss.coupon_service.api.model.RedeemCouponResponse;
import com.github.paulinagazwa.oss.coupon_service.entity.CouponEntity;
import com.github.paulinagazwa.oss.coupon_service.exception.CouponAlreadyExistsException;
import com.github.paulinagazwa.oss.coupon_service.exception.CouponAlreadyRedeemedException;
import com.github.paulinagazwa.oss.coupon_service.exception.CouponCountryMismatchException;
import com.github.paulinagazwa.oss.coupon_service.exception.CouponNotFoundException;
import com.github.paulinagazwa.oss.coupon_service.exception.InvalidDiscountException;
import com.github.paulinagazwa.oss.coupon_service.mapper.CouponMapper;
import com.github.paulinagazwa.oss.coupon_service.repository.CouponRepository;
import com.github.paulinagazwa.oss.coupon_service.service.CouponService;
import com.github.paulinagazwa.oss.coupon_service.service.GeoLocationService;
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

	private final GeoLocationService geoLocationService;

	@Override
	public CouponResponse createCoupon(CreateCouponRequest createCouponRequest) {

		// Check if coupon with the same code already exists
		ensureUniqueName(createCouponRequest.getName());

		// Check if discount is valid (not negative, not greater than 100% for PERCENTAGE type)
		ensureValidDiscount(createCouponRequest.getDiscount(), createCouponRequest.getDiscountType());

		CouponEntity couponEntity = couponMapper.toEntity(createCouponRequest);
		couponEntity.setCreatedAt(OffsetDateTime.now());
		couponEntity.setCurrentRedemptions(0);
		// TODO: Generate Country if not provided in the request (e.g. based on user's locale or IP address)

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
	public RedeemCouponResponse redeemCoupon(UUID couponId, RedeemCouponRequest redeemCouponRequest, String clientIp) {

		// Check if coupon exists
		CouponEntity coupon = couponRepository.findById(couponId)
				.orElseThrow(() -> new CouponNotFoundException(couponId));

		// Check if coupon is still valid
		ensureRedeemable(coupon);

		// Check country (delegated to GeoLocationService)
		ensureValidCountry(coupon, clientIp);

		// Increment and save
		// TODO make this operation atomic to prevent race conditions
		coupon.setCurrentRedemptions(coupon.getCurrentRedemptions() + 1);
		// TODO add userId to the coupon redemptions to prevent multiple redemptions by the same user
		couponRepository.save(coupon);

		return couponMapper.toRedeemResponse(coupon);
	}

	private void ensureRedeemable(CouponEntity coupon) {

		if (coupon.getCurrentRedemptions() >= coupon.getMaxRedemptions()) {
			throw new CouponAlreadyRedeemedException(coupon.getId());
		}
	}

	private void ensureValidCountry(CouponEntity coupon, String clientIp) {

		String resolvedCountry = geoLocationService.resolveCountry(clientIp);
		if (!coupon.getCountry().equalsIgnoreCase(resolvedCountry)) {
			throw new CouponCountryMismatchException(coupon.getCountry(), resolvedCountry);
		}
	}

	@Override
	public @Nullable CouponResponse getCouponById(UUID couponId) {

		return couponRepository.findById(couponId)
				.map(couponMapper::toModel)
				.orElseThrow(() -> new CouponNotFoundException(couponId));
	}

}
