--liquibase formatted sql

--changeset paulinagazwa:003-create-user-coupon-uses-table context:development,production
CREATE SCHEMA IF NOT EXISTS coupon;

CREATE SEQUENCE coupon.user_coupon_uses_seq_id START WITH 1 INCREMENT BY 1;

CREATE TABLE coupon.user_coupon_uses
(
    id          BIGINT      NOT NULL DEFAULT nextval('coupon.user_coupon_uses_seq_id'),
    user_name   VARCHAR(255) NOT NULL,
    redeemed_at TIMESTAMPTZ  NOT NULL,
    coupon_id   UUID         NOT NULL,
    CONSTRAINT pk_user_coupon_uses PRIMARY KEY (id),
    CONSTRAINT fk_user_coupon_uses_coupon FOREIGN KEY (coupon_id) REFERENCES coupons (id),
    CONSTRAINT uq_user_coupon_uses_user_coupon UNIQUE (coupon_id, user_name)
);

--rollback DROP TABLE coupon.user_coupon_uses; DROP SEQUENCE coupon.user_coupon_uses_seq_id;
