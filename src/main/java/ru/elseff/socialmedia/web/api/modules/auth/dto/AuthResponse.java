package ru.elseff.socialmedia.web.api.modules.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Setter
@Getter
@Builder
@ToString
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Авторизационный ответ")
public class AuthResponse {

    @Schema(description = "id пользователя")
    Long id;

    @Schema(description = "Электронная почта пользователя")
    String email;

    @Schema(description = "Имя пользователя")
    String username;

    @Schema(description = "Токен авторизации пользователя")
    String token;
}
