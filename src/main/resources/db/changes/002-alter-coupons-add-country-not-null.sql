--liquibase formatted sql

--changeset paulinagazwa:002-alter-coupons-add-country-not-null context:development,production
UPDATE coupon.coupons SET country = 'PL' WHERE country IS NULL;

ALTER TABLE coupon.coupons
    ALTER COLUMN country SET NOT NULL;

--rollback ALTER TABLE coupon.coupons ALTER COLUMN country DROP NOT NULL;
