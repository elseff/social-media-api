CREATE TABLE post_entity
(
    id      BIGSERIAL    NOT NULL,
    title   VARCHAR(255) NOT NULL,
    text    TEXT         NOT NULL,
    user_id BIGINT       NOT NULL,
    created_at TIMESTAMP DEFAULT now(),
    updated_at TIMESTAMP,
    CONSTRAINT pk_post_id PRIMARY KEY (id),
    CONSTRAINT fk_user_id
        FOREIGN KEY (user_id) REFERENCES user_entity (id)
);