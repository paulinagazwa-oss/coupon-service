package com.github.paulinagazwa.oss.coupon_service.exception;

import com.github.paulinagazwa.oss.coupon_service.api.model.Problem;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private static final String REQUEST_URI = "/api/v1/coupon/test";
    private static final String COUPON_NAME = "SUMMER2024";
    private static final String FIELD_DISCOUNT = "discount";
    private static final String FIELD_MAX_REDEMPTIONS = "maxRedemptions";
    private static final String MSG_MUST_NOT_BE_NULL = "must not be null";
    private static final String MSG_MUST_BE_GREATER_THAN_ZERO = "must be greater than 0";
    private static final String MSG_UNEXPECTED_ERROR = "Unexpected error";

    private GlobalExceptionHandler handler;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        when(request.getRequestURI()).thenReturn(REQUEST_URI);
    }

	@Test
	void shouldReturn400WhenDiscountIsInvalid() {
		InvalidDiscountException ex = new InvalidDiscountException(-5.0f);

		ResponseEntity<Problem> response = handler.handleInvalidDiscount(ex, request);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_PROBLEM_JSON);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
		assertThat(response.getBody().getTitle()).isEqualTo(ProblemTitles.VALIDATION_FAILED);
		assertThat(response.getBody().getDetail()).contains("-5.0");
	}

    @Test
    void shouldReturn404WhenCouponNotFound() {
        UUID couponId = UUID.randomUUID();
        CouponNotFoundException ex = new CouponNotFoundException(couponId);

        ResponseEntity<Problem> response = handler.handleNotFound(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_PROBLEM_JSON);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(response.getBody().getTitle()).isEqualTo(ProblemTitles.NOT_FOUND);
        assertThat(response.getBody().getDetail()).contains(couponId.toString());
        assertThat(response.getBody().getInstance()).isEqualTo(REQUEST_URI);
    }

    @Test
    void shouldReturn409WhenCouponAlreadyExists() {
        CouponAlreadyExistsException ex = new CouponAlreadyExistsException(COUPON_NAME);

        ResponseEntity<Problem> response = handler.handleConflict(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_PROBLEM_JSON);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(response.getBody().getTitle()).isEqualTo(ProblemTitles.CONFLICT);
        assertThat(response.getBody().getDetail()).contains(COUPON_NAME);
    }

    @Test
    void shouldReturn409WhenCouponAlreadyRedeemed() {
        UUID couponId = UUID.randomUUID();
        CouponAlreadyRedeemedException ex = new CouponAlreadyRedeemedException(couponId);

        ResponseEntity<Problem> response = handler.handleAlreadyRedeemed(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_PROBLEM_JSON);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(response.getBody().getTitle()).isEqualTo(ProblemTitles.CONFLICT);
        assertThat(response.getBody().getDetail()).contains(couponId.toString());
    }

	@Test
	void shouldReturn409WhenCouponAlreadyInUse() {
		String exceptionMessage = "Coupon is busy at the moment, try again later.";
		CouponAlreadyInUseException ex = new CouponAlreadyInUseException();

		ResponseEntity<Problem> response = handler.handleAlreadyLock(ex, request);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
		assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_PROBLEM_JSON);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
		assertThat(response.getBody().getTitle()).isEqualTo(ProblemTitles.CONFLICT);
		assertThat(response.getBody().getDetail()).contains(exceptionMessage);
	}

    @Test
    void shouldReturn400WithFieldErrorsWhenValidationFails() {
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(
                new FieldError("createCouponRequest", FIELD_DISCOUNT, MSG_MUST_NOT_BE_NULL),
                new FieldError("createCouponRequest", FIELD_MAX_REDEMPTIONS, MSG_MUST_BE_GREATER_THAN_ZERO)
        ));
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<Problem> response = handler.handleValidation(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_PROBLEM_JSON);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getBody().getTitle()).isEqualTo(ProblemTitles.VALIDATION_FAILED);
        assertThat(response.getBody().getErrors()).hasSize(2);
        assertThat(response.getBody().getErrors())
                .anyMatch(e -> e.getField().equals(FIELD_DISCOUNT) && e.getMessage().equals(MSG_MUST_NOT_BE_NULL))
                .anyMatch(e -> e.getField().equals(FIELD_MAX_REDEMPTIONS) && e.getMessage().equals(MSG_MUST_BE_GREATER_THAN_ZERO));
    }

    @Test
    void shouldReturn500ForUnexpectedException() {
        Exception ex = new RuntimeException(MSG_UNEXPECTED_ERROR);

        ResponseEntity<Problem> response = handler.handleGeneric(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_PROBLEM_JSON);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(response.getBody().getTitle()).isEqualTo(ProblemTitles.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getDetail()).isEqualTo(MSG_UNEXPECTED_ERROR);
    }

	@Test
	void shouldReturn403WhenCouponCountryMismatch() {

		CouponCountryMismatchException ex = new CouponCountryMismatchException("DE", "PL");

		ResponseEntity<Problem> response = handler.handleCountryMismatch(ex, request);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
		assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_PROBLEM_JSON);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
		assertThat(response.getBody().getTitle()).isEqualTo(ProblemTitles.FORBIDDEN);
		assertThat(response.getBody().getDetail()).contains("PL");
	}
}
