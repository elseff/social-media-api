CREATE TABLE message_entity(
    id BIGSERIAL NOT NULL,
    text VARCHAR(1000) NOT NULL,
    sender_id BIGINT NOT NULL,
    recipient_id BIGINT NOT NULL,
    send_at TIMESTAMP DEFAULT now(),
    CONSTRAINT pk_message PRIMARY KEY (id),
    CONSTRAINT  fk_sender_user_id FOREIGN KEY (sender_id) REFERENCES user_entity(id),
    CONSTRAINT fk_recipient_user_id FOREIGN KEY (recipient_id) REFERENCES user_entity(id)
);