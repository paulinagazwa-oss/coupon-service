package com.github.paulinagazwa.oss.coupon_service.repository;

import com.github.paulinagazwa.oss.coupon_service.entity.CouponEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CouponRepository extends JpaRepository<CouponEntity, UUID> {

	boolean existsByName(String name);
}
