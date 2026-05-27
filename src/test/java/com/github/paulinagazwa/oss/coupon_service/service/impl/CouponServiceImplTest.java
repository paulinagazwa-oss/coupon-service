package com.github.paulinagazwa.oss.coupon_service.service.impl;

import com.github.paulinagazwa.oss.coupon_service.api.model.CouponResponse;
import com.github.paulinagazwa.oss.coupon_service.api.model.CreateCouponRequest;
import com.github.paulinagazwa.oss.coupon_service.api.model.DiscountType;
import com.github.paulinagazwa.oss.coupon_service.api.model.RedeemCouponRequest;
import com.github.paulinagazwa.oss.coupon_service.api.model.RedeemCouponResponse;
import com.github.paulinagazwa.oss.coupon_service.entity.CouponEntity;
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
import com.github.paulinagazwa.oss.coupon_service.service.GeoLocationService;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
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

	private static final String BLANK_NAME = "   ";

	private static final String CLIENT_IP = "192.168.1.1";

	private static final String COUPON_COUNTRY = "PL";

	private static final int PARTIAL_REDEMPTIONS = 5;

	private static final String USERNAME = "jan.kowalski";

	@Mock
	private CouponRepository couponRepository;

	@Mock
	private UserCouponUsesRepository userCouponUsesRepository;

	@Mock
	private CouponMapper couponMapper;

	@Mock
	private GeoLocationService geoLocationService;

	@Mock
	private AdvisoryLockRepository advisoryLockRepository;

	@InjectMocks
	private CouponServiceImpl couponService;

	@Test
	void shouldCreateCouponAndReturnResponse() {

		CreateCouponRequest request = requestWithName(VALID_PERCENTAGE, DiscountType.PERCENTAGE);
		CouponEntity entity = entityWithName(VALID_PERCENTAGE, DiscountType.PERCENTAGE);
		CouponResponse expectedResponse = new CouponResponse();

		when(couponRepository.existsByNameIgnoreCase(COUPON_NAME)).thenReturn(false);
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

		when(couponRepository.existsByNameIgnoreCase(COUPON_NAME)).thenReturn(false);
		stubSave(request, entity);

		couponService.createCoupon(request);

		assertThat(captorSaved().getName()).isEqualTo(COUPON_NAME);
	}

	@Test
	void shouldThrowWhenCouponWithSameNameAlreadyExists() {

		CreateCouponRequest request = requestWithName(VALID_PERCENTAGE, DiscountType.PERCENTAGE);
		when(couponRepository.existsByNameIgnoreCase(COUPON_NAME)).thenReturn(true);

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

		when(couponRepository.existsByNameIgnoreCase(BLANK_NAME)).thenReturn(false);
		stubSave(request, entity);

		couponService.createCoupon(request);

		assertThat(captorSaved().getName()).isNotBlank().hasSize(GENERATED_NAME_LENGTH);
	}

	@Test
	void shouldNotCheckNameUniquenessWhenNameIsNull() {

		CreateCouponRequest request = request(SMALL_FIXED_DISCOUNT, DiscountType.FIXED_AMOUNT);
		stubSave(request, entity(SMALL_FIXED_DISCOUNT, DiscountType.FIXED_AMOUNT));

		couponService.createCoupon(request);

		verify(couponRepository, never()).existsByNameIgnoreCase(any());
	}

	@Test
	void shouldRedeemCouponSuccessfully() {

		UUID couponId = UUID.randomUUID();
		CouponEntity coupon = redeemableCoupon(couponId, PARTIAL_REDEMPTIONS);
		RedeemCouponResponse expectedResponse = new RedeemCouponResponse();

		when(couponRepository.findById(couponId)).thenReturn(Optional.of(coupon));
		when(geoLocationService.resolveCountry(CLIENT_IP)).thenReturn(COUPON_COUNTRY);
		when(couponRepository.save(any())).thenReturn(coupon);
		when(couponMapper.toRedeemResponse(eq(coupon), any())).thenReturn(expectedResponse);
		when(advisoryLockRepository.tryToLockId(anyLong())).thenReturn(true);

		assertThat(couponService.redeemCoupon(couponId, new RedeemCouponRequest(USERNAME), CLIENT_IP))
				.isEqualTo(expectedResponse);
	}

	@Test
	void shouldIncrementCurrentRedemptionsWhenRedeeming() {

		UUID couponId = UUID.randomUUID();
		CouponEntity coupon = redeemableCoupon(couponId, PARTIAL_REDEMPTIONS);

		when(couponRepository.findById(couponId)).thenReturn(Optional.of(coupon));
		when(geoLocationService.resolveCountry(CLIENT_IP)).thenReturn(COUPON_COUNTRY);
		when(couponRepository.save(any())).thenReturn(coupon);
		when(couponMapper.toRedeemResponse(any(), any())).thenReturn(new RedeemCouponResponse());
		when(advisoryLockRepository.tryToLockId(anyLong())).thenReturn(true);

		couponService.redeemCoupon(couponId, new RedeemCouponRequest(USERNAME), CLIENT_IP);

		assertThat(captorSaved().getCurrentRedemptions()).isEqualTo(PARTIAL_REDEMPTIONS + 1);
	}

	@Test
	void shouldThrowWhenRedeemingNonExistentCoupon() {

		UUID couponId = UUID.randomUUID();
		when(couponRepository.findById(couponId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> couponService.redeemCoupon(couponId, new RedeemCouponRequest(USERNAME), CLIENT_IP))
				.isInstanceOf(CouponNotFoundException.class);
		verify(couponRepository, never()).save(any());
	}

	@Test
	void shouldThrowWhenCouponReachedMaxRedemptions() {

		UUID couponId = UUID.randomUUID();
		CouponEntity coupon = redeemableCoupon(couponId, MAX_REDEMPTIONS);

		when(couponRepository.findById(couponId)).thenReturn(Optional.of(coupon));

		assertThatThrownBy(() -> couponService.redeemCoupon(couponId, new RedeemCouponRequest(USERNAME), CLIENT_IP))
				.isInstanceOf(CouponAlreadyRedeemedException.class);
		verify(couponRepository, never()).save(any());
	}

	@Test
	void shouldThrowWhenCouponUsedByUser() {

		UUID couponId = UUID.randomUUID();
		CouponEntity coupon = redeemableCoupon(couponId, PARTIAL_REDEMPTIONS);

		when(couponRepository.findById(any())).thenReturn(Optional.of(coupon));
		when(userCouponUsesRepository.existsByCouponIdAndUserNameIgnoreCase(couponId, USERNAME)).thenReturn(true);

		assertThatThrownBy(() -> couponService.redeemCoupon(couponId, new RedeemCouponRequest(USERNAME), CLIENT_IP))
				.isInstanceOf(CouponAlreadyRedeemedException.class);
		verify(couponRepository, never()).save(any());
	}

	@Test
	void shouldThrowWhenLockCannotBeSet() {

		UUID couponId = UUID.randomUUID();
		CouponEntity coupon = redeemableCoupon(couponId, PARTIAL_REDEMPTIONS);

		when(couponRepository.findById(any())).thenReturn(Optional.of(coupon));
		when(geoLocationService.resolveCountry(CLIENT_IP)).thenReturn(COUPON_COUNTRY);
		when(advisoryLockRepository.tryToLockId(anyLong())).thenReturn(false);

		assertThatThrownBy(() -> couponService.redeemCoupon(couponId, new RedeemCouponRequest(USERNAME), CLIENT_IP))
				.isInstanceOf(CouponAlreadyInUseException.class);
		verify(couponRepository, never()).save(any());
	}

	@Test
	void shouldThrowWhenClientCountryDoesNotMatchCouponCountry() {

		UUID couponId = UUID.randomUUID();
		CouponEntity coupon = redeemableCoupon(couponId, PARTIAL_REDEMPTIONS);

		when(couponRepository.findById(couponId)).thenReturn(Optional.of(coupon));
		when(geoLocationService.resolveCountry(CLIENT_IP)).thenReturn("DE");

		assertThatThrownBy(() -> couponService.redeemCoupon(couponId, new RedeemCouponRequest(USERNAME), CLIENT_IP))
				.isInstanceOf(CouponCountryMismatchException.class);
		verify(couponRepository, never()).save(any());
	}

	@Test
	void shouldCheckRedeemabilityBeforeCallingGeoLocationService() {

		UUID couponId = UUID.randomUUID();
		CouponEntity coupon = redeemableCoupon(couponId, MAX_REDEMPTIONS);

		when(couponRepository.findById(couponId)).thenReturn(Optional.of(coupon));

		assertThatThrownBy(() -> couponService.redeemCoupon(couponId, new RedeemCouponRequest(USERNAME), CLIENT_IP))
				.isInstanceOf(CouponAlreadyRedeemedException.class);
		verify(geoLocationService, never()).resolveCountry(any());
	}


	@Test
	void shouldReturnSameHashForSameInput() {
		String input = "test-string";
		UUID couponId = UUID.randomUUID();

		int result1 = CouponServiceImpl.convertStringToLong(input, couponId);
		int result2 = CouponServiceImpl.convertStringToLong(input, couponId);

		assertThat(result1).isEqualTo(result2);
	}

	@Test
	void shouldReturnDifferentHashForDifferentInput() {
		UUID couponId = UUID.randomUUID();

		int result1 = CouponServiceImpl.convertStringToLong("input1", couponId);
		int result2 = CouponServiceImpl.convertStringToLong("input2", couponId);

		assertThat(result1).isNotEqualTo(result2);
	}

	@Test
	void shouldThrowExceptionForNullString() {
		UUID couponId = UUID.randomUUID();

		assertThatThrownBy(() -> CouponServiceImpl.convertStringToLong(null, couponId))
				.isInstanceOf(CouponNotFoundException.class)
				.hasMessageContaining(couponId.toString());
	}

	@Test
	void shouldGenerateHashForEmptyString() {
		UUID couponId = UUID.randomUUID();

		int result = CouponServiceImpl.convertStringToLong("", couponId);

		// tylko sprawdzamy że działa (empty string jest valid)
		assertThat(result).isNotZero();
	}

	@Test
	void shouldBeConsistentAcrossMultipleCalls() {
		String input = "consistent-test";
		UUID couponId = UUID.randomUUID();

		int first = CouponServiceImpl.convertStringToLong(input, couponId);

		for (int i = 0; i < 10; i++) {
			assertThat(CouponServiceImpl.convertStringToLong(input, couponId))
					.isEqualTo(first);
		}
	}

	private CouponEntity redeemableCoupon(UUID id, int currentRedemptions) {

		CouponEntity coupon = new CouponEntity();
		coupon.setId(id);
		coupon.setName(UUID.randomUUID().toString());
		coupon.setCountry(CouponServiceImplTest.COUPON_COUNTRY);
		coupon.setMaxRedemptions(CouponServiceImplTest.MAX_REDEMPTIONS);
		coupon.setCurrentRedemptions(currentRedemptions);
		return coupon;
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
