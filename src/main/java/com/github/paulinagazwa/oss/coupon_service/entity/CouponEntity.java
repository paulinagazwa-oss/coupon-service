package com.github.paulinagazwa.oss.coupon_service.entity;

import com.github.paulinagazwa.oss.coupon_service.api.model.DiscountType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "coupons", schema = "coupon")
@Getter
@Setter
public class CouponEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(nullable = false, updatable = false)
	private UUID id;

	@Column(nullable = true)
	private String name;

	@Column(nullable = false, updatable = false)
	private OffsetDateTime createdAt;

	@Column(nullable = false)
	private Integer maxRedemptions;

	@Column(nullable = false)
	private Float discount;

	@Enumerated(EnumType.ORDINAL)
	@Column(nullable = false)
	private DiscountType discountType;

	@Column(nullable = false)
	private Integer currentRedemptions;

	@Column(length = 2, nullable = false)
	private String country;

	@OneToMany(mappedBy = "coupon", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<UserCouponUsesEntity> userUses = new HashSet<>();
}
