package com.github.paulinagazwa.oss.coupon_service.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AdvisoryLockRepository {

	private final JdbcTemplate jdbcTemplate;

	public boolean tryToLockId(long lockId) {

		return Boolean.TRUE.equals(jdbcTemplate.queryForObject(
				"SELECT pg_try_advisory_xact_lock(?)",
				Boolean.class,
				lockId
		));
	}

}
