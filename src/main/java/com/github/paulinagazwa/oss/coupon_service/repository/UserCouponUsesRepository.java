package com.github.paulinagazwa.oss.coupon_service.repository;

import com.github.paulinagazwa.oss.coupon_service.entity.UserCouponUsesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserCouponUsesRepository extends JpaRepository<UserCouponUsesEntity, Long> {

}
