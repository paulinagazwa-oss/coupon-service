package com.github.paulinagazwa.oss.coupon_service.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.paulinagazwa.oss.coupon_service.service.GeoLocationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

@Service
@Slf4j
public class GeoLocationServiceImpl implements GeoLocationService {

	public static final String UNKNOWN = "UNKNOWN";

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private final RestClient restClient;

	private final String url;

	private final String fields;

	private final String responseFormat;

	public GeoLocationServiceImpl(
			RestClient.Builder restClientBuilder,
			@Value("${geolocation.url}") String url,
			@Value("${geolocation.fields:}") String fields,
			@Value("${geolocation.response-format:plain}") String responseFormat) {

		HttpClient httpClient = HttpClient.newBuilder()
				.connectTimeout(Duration.ofSeconds(2))
				.build();

		JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
		requestFactory.setReadTimeout(Duration.ofSeconds(3));

		this.restClient = restClientBuilder.requestFactory(requestFactory).build();
		this.url = url;
		this.fields = fields;
		this.responseFormat = responseFormat;
	}

	@Override
	public String resolveCountry(String ipAddress) {
		if (ipAddress == null || ipAddress.isBlank()) {
			log.warn("Received null or blank IP address");
			return UNKNOWN;
		}
		try {
			String response = fetchResponse(ipAddress);
			return parseResponse(response, ipAddress);
		} catch (Exception e) {
			log.error("Failed to resolve country for IP: {}, reason: {}", ipAddress, e.getMessage());
			return UNKNOWN;
		}
	}

	private String fetchResponse(String ipAddress) {
		if (fields.isBlank()) {
			return restClient.get().uri(url, ipAddress).retrieve().body(String.class);
		}
		return restClient.get().uri(url + "?fields={fields}", ipAddress, fields).retrieve().body(String.class);
	}

	private String parseResponse(String response, String ipAddress) throws Exception {
		return switch (responseFormat) {
			case "json" -> parseJson(response);
			case "plain" -> parsePlain(response, ipAddress);
			default -> throw new IllegalArgumentException("Unknown response format: " + responseFormat);
		};
	}

	private String parseJson(String response) throws Exception {
		JsonNode root = OBJECT_MAPPER.readTree(response);

		JsonNode statusNode = root.get("status");
		if (statusNode != null && "fail".equals(statusNode.asText())) {
			log.warn("GeoLocation failed: {}", root.get("message"));
			return UNKNOWN;
		}

		JsonNode countryNode = root.get(fields);
		return countryNode != null ? countryNode.asText() : UNKNOWN;
	}

	private String parsePlain(String response, String ipAddress) {
		if (response == null) {
			log.warn("Null response for IP: {}", ipAddress);
			return UNKNOWN;
		}
		String trimmed = response.trim().toUpperCase();
		if (trimmed.length() != 2) {
			log.warn("Unexpected plain response: {}", trimmed);
			return UNKNOWN;
		}
		return trimmed;
	}
}
