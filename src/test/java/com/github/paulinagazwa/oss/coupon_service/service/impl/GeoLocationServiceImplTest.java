package com.github.paulinagazwa.oss.coupon_service.service.impl;

import mockwebserver3.MockResponse;
import mockwebserver3.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class GeoLocationServiceImplTest {

    private static final String VALID_IP = "1.2.3.4";
    private static final String INVALID_IP = "999.999.999.999";
    private static final String COUNTRY_CODE = "PL";
    private static final String UNKNOWN = "UNKNOWN";
    private static final String FORMAT_JSON = "json";
    private static final String FORMAT_PLAIN = "plain";
    private static final String FIELD_COUNTRY_CODE = "countryCode";

    private MockWebServer mockWebServer;
    private GeoLocationServiceImpl service;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.close();
    }

    private GeoLocationServiceImpl createService(String responseFormat, String fields) {
        String baseUrl = mockWebServer.url("/json/{ip}").toString()
                .replace("%7Bip%7D", "{ip}");
        return new GeoLocationServiceImpl(
                RestClient.builder(),
                baseUrl,
                fields,
                responseFormat
        );
    }

    private static MockResponse jsonResponse(String body) {
        return new MockResponse.Builder()
                .code(200)
                .addHeader("Content-Type", "application/json")
                .body(body)
                .build();
    }

    private static MockResponse plainResponse(String body) {
        return new MockResponse.Builder()
                .code(200)
                .body(body)
                .build();
    }

    private static MockResponse errorResponse(int code) {
        return new MockResponse.Builder()
                .code(code)
                .build();
    }

    // -------------------------
    // JSON format
    // -------------------------

    @Test
    void resolveCountry_json_returnsCountryCode() {
        mockWebServer.enqueue(jsonResponse("{\"status\":\"success\",\"countryCode\":\"PL\"}"));

        service = createService(FORMAT_JSON, FIELD_COUNTRY_CODE);
        assertThat(service.resolveCountry(VALID_IP)).isEqualTo(COUNTRY_CODE);
    }

    @Test
    void resolveCountry_json_whenStatusFail_returnsUnknown() {
        mockWebServer.enqueue(jsonResponse("{\"status\":\"fail\",\"message\":\"invalid query\"}"));

        service = createService(FORMAT_JSON, FIELD_COUNTRY_CODE);
        assertThat(service.resolveCountry(INVALID_IP)).isEqualTo(UNKNOWN);
    }

    @Test
    void resolveCountry_json_whenFieldMissing_returnsUnknown() {
        mockWebServer.enqueue(jsonResponse("{\"status\":\"success\"}"));

        service = createService(FORMAT_JSON, FIELD_COUNTRY_CODE);
        assertThat(service.resolveCountry(VALID_IP)).isEqualTo(UNKNOWN);
    }

    @Test
    void resolveCountry_json_whenServerReturns500_returnsUnknown() {
        mockWebServer.enqueue(errorResponse(500));

        service = createService(FORMAT_JSON, FIELD_COUNTRY_CODE);
        assertThat(service.resolveCountry(VALID_IP)).isEqualTo(UNKNOWN);
    }

    @Test
    void resolveCountry_json_whenRateLimited_returnsUnknown() {
        mockWebServer.enqueue(errorResponse(429));

        service = createService(FORMAT_JSON, FIELD_COUNTRY_CODE);
        assertThat(service.resolveCountry(VALID_IP)).isEqualTo(UNKNOWN);
    }

    // -------------------------
    // Plain format
    // -------------------------

    @Test
    void resolveCountry_plain_returnsCountryCode() {
        mockWebServer.enqueue(plainResponse("PL"));

        service = createService(FORMAT_PLAIN, "");
        assertThat(service.resolveCountry(VALID_IP)).isEqualTo(COUNTRY_CODE);
    }

    @Test
    void resolveCountry_plain_lowercaseIsNormalized() {
        mockWebServer.enqueue(plainResponse("pl"));

        service = createService(FORMAT_PLAIN, "");
        assertThat(service.resolveCountry(VALID_IP)).isEqualTo(COUNTRY_CODE);
    }

	@Test
	void resolveCountry_plain_whenResponseNull_returnsUnknown() {
		mockWebServer.enqueue(new MockResponse.Builder().code(200).build());

		service = createService(FORMAT_PLAIN, "");
		assertThat(service.resolveCountry(VALID_IP)).isEqualTo(UNKNOWN);
	}

    @Test
    void resolveCountry_plain_whenResponseNotTwoChars_returnsUnknown() {
        mockWebServer.enqueue(plainResponse("Too Many Requests"));

        service = createService(FORMAT_PLAIN, "");
        assertThat(service.resolveCountry(VALID_IP)).isEqualTo(UNKNOWN);
    }

    // -------------------------
    // IP validation
    // -------------------------

    @Test
    void resolveCountry_whenIpNull_returnsUnknown() {
        service = createService(FORMAT_PLAIN, "");
        assertThat(service.resolveCountry(null)).isEqualTo(UNKNOWN);
    }

    @Test
    void resolveCountry_whenIpBlank_returnsUnknown() {
        service = createService(FORMAT_PLAIN, "");
        assertThat(service.resolveCountry("   ")).isEqualTo(UNKNOWN);
    }

    // -------------------------
    // unknown format
    // -------------------------

    @Test
    void resolveCountry_whenUnknownFormat_returnsUnknown() {
        mockWebServer.enqueue(plainResponse("PL"));

        service = createService("xml", "");
        assertThat(service.resolveCountry(VALID_IP)).isEqualTo(UNKNOWN);
    }
}
