package ru.elseff.socialmedia.web.api.modules.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.elseff.socialmedia.web.api.modules.auth.dto.AuthLoginRequest;
import ru.elseff.socialmedia.web.api.modules.auth.dto.AuthRegisterRequest;
import ru.elseff.socialmedia.web.api.modules.auth.dto.AuthResponse;
import ru.elseff.socialmedia.web.api.modules.auth.service.AuthService;

import javax.validation.Valid;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/auth")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Authentication controller", description = "Управление авторизацией и аутентификацией")
public class AuthController {

    AuthService authService;

    @Operation(
            method = "POST",
            summary = "Регистрация",
            description = "Зарегистрировать новый аккаунт. Выдача токенов авторизации.",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Пользователь успешно зарегистрирован",
                            content = @Content(schema = @Schema(implementation = AuthResponse.class))
                    ),
                    @ApiResponse(responseCode = "400", description = "Неверные пользовательские данные", content = @Content),
            }
    )
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@RequestBody(description = "Данные для регистрации пользователя",
            required = true,
            content = @Content(schema = @Schema(implementation = AuthRegisterRequest.class)))
                                 @org.springframework.web.bind.annotation.RequestBody @Valid
                                         AuthRegisterRequest authRegisterRequest) {
        return authService.register(authRegisterRequest);
    }

    @Operation(
            method = "POST",
            summary = "Войти",
            description = "Вход в аккаунт. Выдача токена авторизации",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            content = @Content(schema = @Schema(implementation = AuthResponse.class))
                    ),
                    @ApiResponse(responseCode = "400", description = "Неверные пользовательские данные", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Пользователь не найден", content = @Content),
            }
    )
    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public AuthResponse login(@RequestBody(description = "Данные для входа",
            required = true,
            content = @Content(schema = @Schema(implementation = AuthLoginRequest.class)))
                              @org.springframework.web.bind.annotation.RequestBody @Valid
                                      AuthLoginRequest authLoginRequest) {
        return authService.login(authLoginRequest);
    }
}
