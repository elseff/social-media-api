package ru.elseff.socialmedia.web.api.modules.message.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.sql.Timestamp;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MessageDto {

    Long id;

    String text;

    Timestamp sendAt;

    String senderUsername;

    String recipientUsername;
}
