CREATE TABLE user_entity_role_entity
(
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    CONSTRAINT fk_user_id FOREIGN KEY (user_id) REFERENCES user_entity (id),
    CONSTRAINT fk_role_id FOREIGN KEY (role_id) REFERENCES role_entity (id),
    CONSTRAINT users_roles_pkey PRIMARY KEY (user_id, role_id)
);

INSERT INTO user_entity_role_entity
VALUES (0, 1),
       (0, 2),
       (1, 1),
       (2, 1),
       (3, 1);