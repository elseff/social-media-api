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
public class AuthLoginRequest {

    @Email(message = "Электронная почта должна быть действительной")
    String email;

    @Size(min = 5, max = 40)
    String username;

    @NotNull(message = "Поле пароль не должно бытб пустым")
    @Size(min = 4, message = "Длина пароля должна быть не менее 4 символов")
    String password;
}
