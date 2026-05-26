package com.github.paulinagazwa.oss.coupon_service.controller;

import com.github.paulinagazwa.oss.coupon_service.api.model.CouponResponse;
import com.github.paulinagazwa.oss.coupon_service.api.model.CreateCouponRequest;
import com.github.paulinagazwa.oss.coupon_service.api.model.DiscountType;
import com.github.paulinagazwa.oss.coupon_service.api.model.RedeemCouponRequest;
import com.github.paulinagazwa.oss.coupon_service.api.model.RedeemCouponResponse;
import com.github.paulinagazwa.oss.coupon_service.service.CouponService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CouponControllerTest {

    private static final String CLIENT_IP = "1.2.3.4";

    private static final String USERNAME = "jan.kowalski";

    @Mock
    private CouponService couponService;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private CouponController couponController;

    @Test
    void registerCoupon_returnsOkWithCouponResponse() {
        CreateCouponRequest request = new CreateCouponRequest(15.0f, DiscountType.PERCENTAGE, 100);
        CouponResponse expectedResponse = new CouponResponse();

        when(couponService.createCoupon(request)).thenReturn(expectedResponse);

        ResponseEntity<CouponResponse> response = couponController.registerCoupon(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedResponse);
    }

    @Test
    void getCouponById_returnsOkWithCouponResponse() {
        UUID couponId = UUID.randomUUID();
        CouponResponse expectedResponse = new CouponResponse();

        when(couponService.getCouponById(couponId)).thenReturn(expectedResponse);

        ResponseEntity<CouponResponse> response = couponController.getCouponById(couponId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedResponse);
    }

    @Test
    void redeemCoupon_returnsCreatedWithRedeemResponse() {
        UUID couponId = UUID.randomUUID();
        RedeemCouponRequest request = new RedeemCouponRequest(USERNAME);
        RedeemCouponResponse expectedResponse = new RedeemCouponResponse();

        when(httpServletRequest.getHeader("X-Forwarded-For")).thenReturn(null);
        when(httpServletRequest.getRemoteAddr()).thenReturn(CLIENT_IP);
        when(couponService.redeemCoupon(couponId, request, CLIENT_IP)).thenReturn(expectedResponse);

        ResponseEntity<RedeemCouponResponse> response = couponController.redeemCoupon(couponId, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(expectedResponse);
    }

    @Test
    void redeemCoupon_usesFirstIpFromXForwardedForHeader() {
        UUID couponId = UUID.randomUUID();
        RedeemCouponRequest request = new RedeemCouponRequest(USERNAME);
        RedeemCouponResponse expectedResponse = new RedeemCouponResponse();

        when(httpServletRequest.getHeader("X-Forwarded-For")).thenReturn("1.2.3.4, 5.6.7.8, 9.10.11.12");
        when(couponService.redeemCoupon(couponId, request, CLIENT_IP)).thenReturn(expectedResponse);

        ResponseEntity<RedeemCouponResponse> response = couponController.redeemCoupon(couponId, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(expectedResponse);
    }

    @Test
    void redeemCoupon_fallsBackToRemoteAddrWhenXForwardedForIsBlank() {
        UUID couponId = UUID.randomUUID();
        RedeemCouponRequest request = new RedeemCouponRequest(USERNAME);
        RedeemCouponResponse expectedResponse = new RedeemCouponResponse();

        when(httpServletRequest.getHeader("X-Forwarded-For")).thenReturn("   ");
        when(httpServletRequest.getRemoteAddr()).thenReturn(CLIENT_IP);
        when(couponService.redeemCoupon(couponId, request, CLIENT_IP)).thenReturn(expectedResponse);

        ResponseEntity<RedeemCouponResponse> response = couponController.redeemCoupon(couponId, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(expectedResponse);
    }
}
