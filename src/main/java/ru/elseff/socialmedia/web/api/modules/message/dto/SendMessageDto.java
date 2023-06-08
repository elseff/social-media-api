package ru.elseff.socialmedia.web.api.modules.message.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SendMessageDto {

    @NotNull(message = "Поле текст не должно быть пустым")
    @NotBlank(message = "Поле текст должно содержать символы")
    @Size(max = 1000, message = "Длина сообщения не должна превышать 1000 символов")
    String text;

}
