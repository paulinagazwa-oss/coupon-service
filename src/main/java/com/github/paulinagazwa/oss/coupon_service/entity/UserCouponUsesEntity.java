package com.github.paulinagazwa.oss.coupon_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "user_coupon_uses", schema = "coupon",
		uniqueConstraints = @UniqueConstraint(columnNames = {"coupon_id", "user_name"}))
@Getter
@Setter
public class UserCouponUsesEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_coupon_uses_seq")
	@SequenceGenerator(name = "user_coupon_uses_seq", sequenceName = "coupon.user_coupon_uses_seq_id", allocationSize = 1)
	private Long id;

	@Column(nullable = false)
	private String userName;

	@Column(nullable = false)
	private OffsetDateTime redeemedAt;

	@ManyToOne
	@JoinColumn(name = "coupon_id", nullable = false)
	private CouponEntity coupon;

}
