CREATE TABLE post_image_entity
(
    id       BIGSERIAL NOT NULL,
    filename TEXT      NOT NULL,
    post_id  BIGINT    NOT NULL,
    CONSTRAINT pk_post_image_id PRIMARY KEY (id),
    CONSTRAINT fk_post_id FOREIGN KEY (post_id) REFERENCES post_entity (id)
);