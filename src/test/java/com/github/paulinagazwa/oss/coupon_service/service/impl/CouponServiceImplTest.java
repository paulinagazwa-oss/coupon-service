package com.github.paulinagazwa.oss.coupon_service.service.impl;

import com.github.paulinagazwa.oss.coupon_service.api.model.CouponResponse;
import com.github.paulinagazwa.oss.coupon_service.api.model.CreateCouponRequest;
import com.github.paulinagazwa.oss.coupon_service.api.model.DiscountType;
import com.github.paulinagazwa.oss.coupon_service.entity.CouponEntity;
import com.github.paulinagazwa.oss.coupon_service.exception.CouponAlreadyExistsException;
import com.github.paulinagazwa.oss.coupon_service.exception.CouponNotFoundException;
import com.github.paulinagazwa.oss.coupon_service.exception.InvalidDiscountException;
import com.github.paulinagazwa.oss.coupon_service.mapper.CouponMapper;
import com.github.paulinagazwa.oss.coupon_service.repository.CouponRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CouponServiceImplTest {

	private static final String COUPON_NAME = "SUMMER2024";

	private static final float VALID_PERCENTAGE = 15.0f;

	private static final float VALID_FIXED_AMOUNT = 150.0f;

	private static final float SMALL_FIXED_DISCOUNT = 10.0f;

	private static final float NEGATIVE_DISCOUNT = -1.0f;

	private static final float PERCENTAGE_ABOVE_MAX = 101.0f;

	private static final float PERCENTAGE_AT_MAX = 100.0f;

	private static final int MAX_REDEMPTIONS = 100;

	private static final int GENERATED_NAME_LENGTH = 8;

	public static final String BLANK_NAME = "   ";

	@Mock
	private CouponRepository couponRepository;

	@Mock
	private CouponMapper couponMapper;

	@InjectMocks
	private CouponServiceImpl couponService;

	@Test
	void shouldCreateCouponAndReturnResponse() {

		CreateCouponRequest request = requestWithName(VALID_PERCENTAGE, DiscountType.PERCENTAGE);
		CouponEntity entity = entityWithName(VALID_PERCENTAGE, DiscountType.PERCENTAGE);
		CouponResponse expectedResponse = new CouponResponse();

		when(couponRepository.existsByName(COUPON_NAME)).thenReturn(false);
		when(couponMapper.toEntity(request)).thenReturn(entity);
		when(couponRepository.save(any())).thenReturn(entity);
		when(couponMapper.toModel(entity)).thenReturn(expectedResponse);

		assertThat(couponService.createCoupon(request)).isEqualTo(expectedResponse);
	}

	@Test
	void shouldSetCreatedAtAndCurrentRedemptionsWhenCreatingCoupon() {

		CreateCouponRequest request = request(SMALL_FIXED_DISCOUNT, DiscountType.FIXED_AMOUNT);
		CouponEntity entity = entity(SMALL_FIXED_DISCOUNT, DiscountType.FIXED_AMOUNT);
		stubSave(request, entity);

		couponService.createCoupon(request);

		CouponEntity saved = captorSaved();
		assertThat(saved.getCreatedAt()).isNotNull().isBeforeOrEqualTo(OffsetDateTime.now());
		assertThat(saved.getCurrentRedemptions()).isEqualTo(0);
	}

	@Test
	void shouldGenerateNameWhenNotProvidedInRequest() {

		CreateCouponRequest request = request(SMALL_FIXED_DISCOUNT, DiscountType.FIXED_AMOUNT);
		CouponEntity entity = entity(SMALL_FIXED_DISCOUNT, DiscountType.FIXED_AMOUNT);
		stubSave(request, entity);

		couponService.createCoupon(request);

		assertThat(captorSaved().getName()).isNotBlank().hasSize(GENERATED_NAME_LENGTH);
	}

	@Test
	void shouldNotOverrideNameWhenAlreadyProvided() {

		CreateCouponRequest request = requestWithName(SMALL_FIXED_DISCOUNT, DiscountType.FIXED_AMOUNT);
		CouponEntity entity = entityWithName(SMALL_FIXED_DISCOUNT, DiscountType.FIXED_AMOUNT);

		when(couponRepository.existsByName(COUPON_NAME)).thenReturn(false);
		stubSave(request, entity);

		couponService.createCoupon(request);

		assertThat(captorSaved().getName()).isEqualTo(COUPON_NAME);
	}

	@Test
	void shouldThrowWhenCouponWithSameNameAlreadyExists() {

		CreateCouponRequest request = requestWithName(VALID_PERCENTAGE, DiscountType.PERCENTAGE);
		when(couponRepository.existsByName(COUPON_NAME)).thenReturn(true);

		assertThatThrownBy(() -> couponService.createCoupon(request))
				.isInstanceOf(CouponAlreadyExistsException.class);
		verify(couponRepository, never()).save(any());
	}

	@Test
	void shouldThrowWhenDiscountIsNegative() {

		assertThatThrownBy(() -> couponService.createCoupon(request(NEGATIVE_DISCOUNT, DiscountType.PERCENTAGE)))
				.isInstanceOf(InvalidDiscountException.class);
		verify(couponRepository, never()).save(any());
	}

	@Test
	void shouldThrowWhenPercentageDiscountExceeds100() {

		assertThatThrownBy(() -> couponService.createCoupon(request(PERCENTAGE_ABOVE_MAX, DiscountType.PERCENTAGE)))
				.isInstanceOf(InvalidDiscountException.class);
		verify(couponRepository, never()).save(any());
	}

	@Test
	void shouldAllowFixedDiscountGreaterThan100() {

		CreateCouponRequest request = request(VALID_FIXED_AMOUNT, DiscountType.FIXED_AMOUNT);
		stubSave(request, entity(VALID_FIXED_AMOUNT, DiscountType.FIXED_AMOUNT));

		couponService.createCoupon(request);

		verify(couponRepository).save(any());
	}

	@Test
	void shouldAllowPercentageDiscountExactly100() {

		CreateCouponRequest request = request(PERCENTAGE_AT_MAX, DiscountType.PERCENTAGE);
		stubSave(request, entity(PERCENTAGE_AT_MAX, DiscountType.PERCENTAGE));

		couponService.createCoupon(request);

		verify(couponRepository).save(any());
	}

	@Test
	void shouldReturnCouponWhenFoundById() {

		UUID couponId = UUID.randomUUID();
		CouponEntity entity = entity(VALID_PERCENTAGE, DiscountType.PERCENTAGE);
		CouponResponse expectedResponse = new CouponResponse();

		when(couponRepository.findById(couponId)).thenReturn(Optional.of(entity));
		when(couponMapper.toModel(entity)).thenReturn(expectedResponse);

		assertThat(couponService.getCouponById(couponId)).isEqualTo(expectedResponse);
	}

	@Test
	void shouldThrowWhenCouponNotFoundById() {

		UUID couponId = UUID.randomUUID();
		when(couponRepository.findById(couponId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> couponService.getCouponById(couponId))
				.isInstanceOf(CouponNotFoundException.class);
	}

	@Test
	void shouldGenerateNameWhenNameIsBlank() {
		CreateCouponRequest request = request(SMALL_FIXED_DISCOUNT, DiscountType.FIXED_AMOUNT);
		request.setName(BLANK_NAME);

		CouponEntity entity = entity(SMALL_FIXED_DISCOUNT, DiscountType.FIXED_AMOUNT);
		entity.setName(BLANK_NAME);

		when(couponRepository.existsByName(BLANK_NAME)).thenReturn(false);
		stubSave(request, entity);

		couponService.createCoupon(request);

		assertThat(captorSaved().getName()).isNotBlank().hasSize(GENERATED_NAME_LENGTH);
	}

	@Test
	void shouldNotCheckNameUniquenessWhenNameIsNull() {

		CreateCouponRequest request = request(SMALL_FIXED_DISCOUNT, DiscountType.FIXED_AMOUNT);
		stubSave(request, entity(SMALL_FIXED_DISCOUNT, DiscountType.FIXED_AMOUNT));

		couponService.createCoupon(request);

		verify(couponRepository, never()).existsByName(any());
	}

	private CreateCouponRequest request(float discount, DiscountType type) {

		return new CreateCouponRequest(discount, type, MAX_REDEMPTIONS);
	}

	private CreateCouponRequest requestWithName(float discount, DiscountType type) {

		CreateCouponRequest request = request(discount, type);
		request.setName(CouponServiceImplTest.COUPON_NAME);
		return request;
	}

	private CouponEntity entity(float discount, DiscountType type) {

		CouponEntity entity = new CouponEntity();
		entity.setDiscount(discount);
		entity.setDiscountType(type);
		return entity;
	}

	private CouponEntity entityWithName(float discount, DiscountType type) {

		CouponEntity entity = entity(discount, type);
		entity.setName(CouponServiceImplTest.COUPON_NAME);
		return entity;
	}

	private void stubSave(CreateCouponRequest request, CouponEntity entity) {

		when(couponMapper.toEntity(request)).thenReturn(entity);
		when(couponRepository.save(any())).thenReturn(entity);
		when(couponMapper.toModel(any())).thenReturn(new CouponResponse());
	}

	private CouponEntity captorSaved() {

		ArgumentCaptor<CouponEntity> captor = ArgumentCaptor.forClass(CouponEntity.class);
		verify(couponRepository).save(captor.capture());
		return captor.getValue();
	}
}
