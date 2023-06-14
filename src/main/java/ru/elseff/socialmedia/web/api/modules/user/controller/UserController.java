package ru.elseff.socialmedia.web.api.modules.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.elseff.socialmedia.persistense.UserEntity;
import ru.elseff.socialmedia.web.api.modules.user.dto.UserDto;
import ru.elseff.socialmedia.web.api.modules.user.dto.mapper.UserDtoMapper;
import ru.elseff.socialmedia.web.api.modules.user.service.UserService;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/users")
@SecurityRequirement(name = "Bearer Authentication")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "User Controller", description = "Управление пользователями")
public class UserController {

    UserService userService;

    UserDtoMapper userDtoMapper;

    @Operation(
            method = "GET",
            summary = "Профиль пользователя",
            description = "Профиль текущего пользователя",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = " Пользователь успешно найден",
                            content = @Content(
                                    schema = @Schema(implementation = UserDto.class)
                            )
                    )
            }
    )
    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    public UserDto getMe() {
        UserEntity me = userService.getCurrentAuthUser();

        return userDtoMapper.mapUserEntityToDto(me);
    }

    @Operation(
            method = "GET",
            summary = "Друзья пользователя",
            description = "Друзья текущего пользователя",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = " Пользователь успешно найден",
                            content = @Content(
                                    schema = @Schema(implementation = UserDto.class)
                            )
                    )
            }
    )
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/me/friends")
    public Set<UserDto> findFriends() {
        UserEntity user = userService.getCurrentAuthUser();
        List<UserEntity> friends = userService.findFriendsByUsername(user.getUsername());
        return friends
                .stream()
                .map(usr -> {
                    UserDto dto = userDtoMapper.mapUserEntityToDto(usr);
                    dto.setEmail(null);
                    dto.setPassword(null);
                    dto.setRoles(null);
                    dto.setMessages(null);
                    return dto;
                })
                .collect(Collectors.toSet());
    }
}
