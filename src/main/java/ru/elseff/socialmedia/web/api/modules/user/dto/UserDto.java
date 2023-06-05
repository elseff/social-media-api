package ru.elseff.socialmedia.web.api.modules.user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.validation.annotation.Validated;
import ru.elseff.socialmedia.web.api.modules.message.dto.MessageDto;
import ru.elseff.socialmedia.web.api.modules.post.dto.PostDto;
import ru.elseff.socialmedia.web.api.modules.role.dto.RoleDto;
import ru.elseff.socialmedia.web.api.modules.subscription.dto.SubscriptionDto;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@Builder
@ToString
@Validated
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserDto {

    Long id;

    String username;

    String email;

    String password;

    Set<RoleDto> roles;

    List<PostDto> posts;

    List<SubscriptionDto> subscriptions;

    List<SubscriptionDto> subscribers;

    List<MessageDto> messages;
}
