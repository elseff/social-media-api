package ru.elseff.socialmedia.web.api.modules;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Welcome Controller", description = "Приветствие")
public class WelcomeController {

    @Operation(
            summary = "Приветствие",
            description = "Проверка на правильность аутентификации пользоваетеля",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            content = @Content(
                                    schema = @Schema(implementation = String.class)
                            )
                    )
            }
    )
    @GetMapping("/welcome")
    public String hello(Principal principal) {
        String username = principal.getName();
        return String.format("%s успешно вошёл и имеет доступ к  API!!!", username);
    }
}
