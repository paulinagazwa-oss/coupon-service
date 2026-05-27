package com.github.paulinagazwa.oss.coupon_service.service.impl;

import com.github.paulinagazwa.oss.coupon_service.api.model.CouponResponse;
import com.github.paulinagazwa.oss.coupon_service.api.model.CreateCouponRequest;
import com.github.paulinagazwa.oss.coupon_service.api.model.DiscountType;
import com.github.paulinagazwa.oss.coupon_service.api.model.RedeemCouponRequest;
import com.github.paulinagazwa.oss.coupon_service.api.model.RedeemCouponResponse;
import com.github.paulinagazwa.oss.coupon_service.entity.CouponEntity;
import com.github.paulinagazwa.oss.coupon_service.entity.UserCouponUsesEntity;
import com.github.paulinagazwa.oss.coupon_service.exception.CouponAlreadyExistsException;
import com.github.paulinagazwa.oss.coupon_service.exception.CouponAlreadyInUseException;
import com.github.paulinagazwa.oss.coupon_service.exception.CouponAlreadyRedeemedException;
import com.github.paulinagazwa.oss.coupon_service.exception.CouponCountryMismatchException;
import com.github.paulinagazwa.oss.coupon_service.exception.CouponNotFoundException;
import com.github.paulinagazwa.oss.coupon_service.exception.InvalidDiscountException;
import com.github.paulinagazwa.oss.coupon_service.mapper.CouponMapper;
import com.github.paulinagazwa.oss.coupon_service.repository.AdvisoryLockRepository;
import com.github.paulinagazwa.oss.coupon_service.repository.CouponRepository;
import com.github.paulinagazwa.oss.coupon_service.repository.UserCouponUsesRepository;
import com.github.paulinagazwa.oss.coupon_service.service.CouponService;
import com.github.paulinagazwa.oss.coupon_service.service.GeoLocationService;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

	private final CouponRepository couponRepository;

	private final UserCouponUsesRepository userCouponUsesRepository;

	private final CouponMapper couponMapper;

	private final GeoLocationService geoLocationService;

	private final AdvisoryLockRepository advisoryLockRepository;

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

		if (name != null && couponRepository.existsByNameIgnoreCase(name)) {
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

	@Transactional
	@Override
	public RedeemCouponResponse redeemCoupon(UUID couponId, RedeemCouponRequest redeemCouponRequest, String clientIp) {

		// Check if coupon exists
		CouponEntity coupon = couponRepository.findById(couponId)
				.orElseThrow(() -> new CouponNotFoundException(couponId));

		// Check if coupon is still valid
		ensureRedeemable(coupon);

		// Check if coupon can be used per user
		ensureUserRedeemable(couponId, redeemCouponRequest);

		// Check country, delegated to GeoLocationService
		// always check as last, to avoid unnecessary calls to GeoLocationService
		// TODO check localhost - "Unknown" error
		ensureValidCountry(coupon, clientIp);

		if (advisoryLockRepository.tryToLockId(convertStringToLong(coupon.getName(), couponId))) {

			updateDatabase(redeemCouponRequest, coupon);
		} else {
			throw new CouponAlreadyInUseException();
		}

		return couponMapper.toRedeemResponse(coupon, redeemCouponRequest.getUsername());
	}

	private void updateDatabase(RedeemCouponRequest redeemCouponRequest, CouponEntity coupon) {

		Set<UserCouponUsesEntity> userUsesSet = coupon.getUserUses();

		UserCouponUsesEntity userUse = new UserCouponUsesEntity();
		userUse.setUserName(redeemCouponRequest.getUsername());
		userUse.setRedeemedAt(OffsetDateTime.now());
		userUse.setCoupon(coupon);

		userUsesSet.add(userUse);

		coupon.setCurrentRedemptions(coupon.getCurrentRedemptions() + 1);
		couponRepository.save(coupon);
	}

	private void ensureUserRedeemable(UUID couponId, RedeemCouponRequest redeemCouponRequest) {

		String username = redeemCouponRequest.getUsername();

		if (userCouponUsesRepository.existsByCouponIdAndUserNameIgnoreCase(couponId, username)) {
			throw new CouponAlreadyRedeemedException(couponId);
		}
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

	public static int convertStringToLong(String str, UUID couponId) {

		if (str == null) {
			throw new CouponNotFoundException(couponId);
		}

		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
		byte[] hashBytes = md.digest(str.getBytes());
		return Arrays.hashCode(hashBytes);
	}

	@Override
	public @Nullable CouponResponse getCouponById(UUID couponId) {

		return couponRepository.findById(couponId)
				.map(couponMapper::toModel)
				.orElseThrow(() -> new CouponNotFoundException(couponId));
	}

}
