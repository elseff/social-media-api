package ru.elseff.socialmedia.web.api.modules.message.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SendMessageDto {

    @Size(max = 1000, message = "Длина сообщения не должна превышать 1000 символов")
    @NotNull(message = "Поле текст не должно быть пустым")
    String text;

}
