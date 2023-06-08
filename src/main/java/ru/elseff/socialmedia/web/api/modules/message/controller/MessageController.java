package ru.elseff.socialmedia.web.api.modules.message.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.elseff.socialmedia.persistense.MessageEntity;
import ru.elseff.socialmedia.web.api.modules.message.dto.MessageDto;
import ru.elseff.socialmedia.web.api.modules.message.dto.SendMessageDto;
import ru.elseff.socialmedia.web.api.modules.message.dto.mapper.MessageDtoMapper;
import ru.elseff.socialmedia.web.api.modules.message.service.MessageService;
import ru.elseff.socialmedia.web.api.modules.user.service.UserService;

import javax.validation.Valid;
import java.util.Set;
import java.util.stream.Collectors;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/messages")
@SecurityRequirement(name = "Bearer Authentication")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Message Controller", description = "Управление сообщениями")
public class MessageController {

    MessageDtoMapper messageDtoMapper;

    MessageService messageService;

    UserService userService;

    @Operation(
            method = "GET",
            summary = "Посмотреть все сообщения",
            description = "Посмотреть все входящие сообщения для текущего пользователя",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Успешно",
                            content = @Content(schema = @Schema(implementation = MessageDto.class))
                    )
            }
    )
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Set<MessageDto> findAll() {
        Set<MessageEntity> messages = messageService.findAllByRecipient(userService.getCurrentAuthUser());

        return messages
                .stream()
                .map(messageDtoMapper::mapMessageEntityToDto)
                .collect(Collectors.toSet());
    }

    @Operation(
            method = "GET",
            summary = "Получить сообщения от конкретного пользователя",
            description = "Указав имя собеседника, можно получить только его сообщения",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Успешно",
                            content = @Content(schema = @Schema(implementation = MessageDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Пользователь не найден",
                            content = @Content
                    )
            }
    )
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{senderUsername}")
    public Set<MessageDto> findAllBySender(@Parameter(description = "Имя пользователя", required = true)
                                           @PathVariable("senderUsername") String senderUsername) {
        Set<MessageEntity> messages = messageService.findAllBySenderUsername(senderUsername);

        return messages
                .stream()
                .map(messageDtoMapper::mapMessageEntityToDto)
                .collect(Collectors.toSet());
    }

    @Operation(
            method = "POST",
            summary = "Отправить сообщение",
            description = "Указав имя получателя, можно отправить сообщение",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            content = @Content(schema = @Schema(implementation = MessageDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Пользователь не найден",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Неверные данные для отправки",
                            content = @Content
                    )
            }
    )
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/send/{recipientUsername}")
    public MessageDto sendMessage(@Parameter(name = "Имя получателя", required = true)
                                  @PathVariable("recipientUsername") String recipientUsername,
                                  @RequestBody(
                                          description = "Сообщение для отправки",
                                          required = true,
                                          content = @Content(
                                                  schema = @Schema(
                                                          implementation = SendMessageDto.class
                                                  )
                                          )
                                  )
                                  @org.springframework.web.bind.annotation.RequestBody @Valid SendMessageDto message) {
        MessageEntity messageEntity = messageService.sendMessageToUserByUsername(message, recipientUsername);

        return messageDtoMapper.mapMessageEntityToDto(messageEntity);
    }
}
