--liquibase formatted sql

--changeset paulinagazwa:001-create-coupons-table context:development,production
CREATE TABLE coupons
(
    id                  UUID         NOT NULL DEFAULT gen_random_uuid(),
    name                VARCHAR(255),
    created_at          TIMESTAMPTZ  NOT NULL,
    max_redemptions     INT          NOT NULL,
    discount            FLOAT        NOT NULL,
    discount_type       SMALLINT     NOT NULL,
    current_redemptions INT          NOT NULL DEFAULT 0,
    country             VARCHAR(2),
    CONSTRAINT pk_coupons PRIMARY KEY (id),
    CONSTRAINT chk_max_redemptions CHECK (max_redemptions >= 1),
    CONSTRAINT chk_current_redemptions CHECK (current_redemptions >= 0),
    CONSTRAINT chk_discount CHECK (discount > 0),
    CONSTRAINT chk_discount_type CHECK (discount_type IN (0, 1))
);

--rollback DROP TABLE coupons;
