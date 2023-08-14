CREATE TABLE user_entity
(
    id       BIGSERIAL    NOT NULL,
    username VARCHAR(255) NOT NULL,
    email    VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    CONSTRAINT pk_user PRIMARY KEY (id),
    CONSTRAINT uq_user_email UNIQUE (email),
    CONSTRAINT uq_user_username UNIQUE (username)
);

INSERT INTO user_entity(id, username, email, password)
VALUES (0,
        'admin',
        'admin@admin.com',
        '$2a$12$tbVMEjv2G61M5ucrHW0ljeHK6ZHxRj9qo2XRjbIzk5T7Zq3Ld/7Wy');

INSERT INTO user_entity (username, email, password)
VALUES ('elseff',
        'elseff@gmail.com',
        '$2a$12$RajVP3Niv3Mj8zabrJ10weE24CYFWzy75V2gTOPtKmlsnyiXjcYpC'); -- 1234

INSERT INTO user_entity (username, email, password)
VALUES ('willson',
        'willson@gmail.com',
        '$2a$12$RajVP3Niv3Mj8zabrJ10weE24CYFWzy75V2gTOPtKmlsnyiXjcYpC'); -- 1234
INSERT INTO user_entity (username, email, password)
VALUES ('Jacky',
        'jacky@gmail.com',
        '$2a$12$RajVP3Niv3Mj8zabrJ10weE24CYFWzy75V2gTOPtKmlsnyiXjcYpC'); --1234

