CREATE TABLE subscription_entity
(
    user_id       bigint not null,
    subscriber_id bigint not null,
    accepted boolean not null default false,
    CONSTRAINT pk_subscription_id PRIMARY KEY (user_id, subscriber_id),
    CONSTRAINT uq_subscription UNIQUE (user_id, subscriber_id)
);