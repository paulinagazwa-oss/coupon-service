package com.github.paulinagazwa.oss.coupon_service.mapper;

import com.github.paulinagazwa.oss.coupon_service.api.model.CouponResponse;
import com.github.paulinagazwa.oss.coupon_service.api.model.CreateCouponRequest;
import com.github.paulinagazwa.oss.coupon_service.api.model.RedeemCouponResponse;
import com.github.paulinagazwa.oss.coupon_service.entity.CouponEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface CouponMapper {

	CouponResponse toModel(CouponEntity couponEntity);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "currentRedemptions", ignore = true)
	@Mapping(target = "userUses", ignore = true)
	CouponEntity toEntity(CreateCouponRequest couponResponse);

	@Mapping(target = "redeemedAt", expression = "java(java.time.OffsetDateTime.now())")
	@Mapping(target = "redeemedBy", ignore = true)
	RedeemCouponResponse toRedeemResponse(CouponEntity coupon);
}
