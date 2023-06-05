package ru.elseff.socialmedia.web.api.modules.subscription.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.elseff.socialmedia.web.api.modules.subscription.dto.SubscriptionResponse;
import ru.elseff.socialmedia.web.api.modules.subscription.service.SubscriptionService;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Subscription Controller", description = "Управление подписками")
@RequestMapping(value = "/api/v1/subscriptions", consumes = "application/json", produces = "application/json")
public class SubscriptionController {

    SubscriptionService subscriptionService;

    @Operation(
            method = "POST",
            summary = "Изменить статус подписки",
            description = "Изменить подписку на конкретного пользователя (Подписаться/Отписаться)",
            responses = {
                    @ApiResponse(
                            responseCode = "202",
                            description = "Подписка успешно изменена",
                            content = @Content(
                                    schema = @Schema(implementation = SubscriptionResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Пользователь не найден",
                            content = @Content
                    )
            }
    )
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PostMapping("/change-subscription/{username}")
    public SubscriptionResponse changeSub(@Parameter(description = "Имя пользователя", required = true)
                                          @PathVariable(name = "username") String username) {
        return SubscriptionResponse.builder()
                .subscriptionStatus(subscriptionService.changeSub(username))
                .build();
    }

    @Operation(
            method = "POST",
            summary = "Подтвердить подписку",
            description = "Подтвердить подписку пользователя",
            responses = {
                    @ApiResponse(
                            responseCode = "202",
                            description = "Подписка успешно подтверждена",
                            content = @Content(
                                    schema = @Schema(implementation = SubscriptionResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Пользователь не найден",
                            content = @Content
                    )
            }
    )
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PostMapping("/accept/{username}")
    public SubscriptionResponse acceptSubscription(@Parameter(description = "Имя пользователя", required = true)
                                                   @PathVariable(name = "username") String subscriberUsername) {
        return SubscriptionResponse.builder()
                .subscriptionStatus(subscriptionService.acceptSubscription(subscriberUsername))
                .build();
    }
}
