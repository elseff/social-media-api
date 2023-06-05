package ru.elseff.socialmedia.web.api.modules.auth.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Setter
@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthRegisterRequest {

    @Email(message = "Поле Электронная почта должна быть действительной")
    @NotNull(message = "Поле Электронная почта не должно быть пустым")
    String email;

    @Size(min = 5, max = 40, message = "Размер имени должен быть не меньше 5 и не больше 40 символов")
    @NotNull(message = "Поле имя не должно быть пустым")
    String username;

    @NotNull(message = "Поле пароль не должно быть пустым")
    @Size(min = 4, message = "Длина пароля должна превышать 4 символа")
    String password;
}
