package ru.elseff.socialmedia.web.api.modules.user.dto.mapper;

import org.springframework.stereotype.Component;
import ru.elseff.socialmedia.persistense.UserEntity;
import ru.elseff.socialmedia.web.api.modules.auth.dto.AuthRegisterRequest;
import ru.elseff.socialmedia.web.api.modules.message.dto.MessageDto;
import ru.elseff.socialmedia.web.api.modules.post.dto.PostDto;
import ru.elseff.socialmedia.web.api.modules.role.dto.RoleDto;
import ru.elseff.socialmedia.web.api.modules.subscription.dto.SubscriptionDto;
import ru.elseff.socialmedia.web.api.modules.user.dto.UserDto;

import java.util.HashSet;
import java.util.stream.Collectors;

@Component
public class UserDtoMapper {
    public UserDto mapUserEntityToDto(UserEntity user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(user.getRoles().stream().map(role ->
                        RoleDto.builder()
                                .name(role.getName())
                                .build())
                        .collect(Collectors.toSet()))
                .posts(user.getPosts().stream().map(post ->
                        PostDto.builder()
                                .id(post.getId())
                                .title(post.getTitle())
                                .text(post.getText())
                                .build()).collect(Collectors.toList()))
                .subscribers(user.getSubscribers().stream().map(subscription ->
                        SubscriptionDto.builder()
                                .subscriberUsername(subscription.getSubscriber().getUsername())
                                .accepted(subscription.getAccepted())
                                .build())
                        .collect(Collectors.toList()))
                .subscriptions(user.getSubscriptions().stream().map(sub ->
                        SubscriptionDto.builder()
                                .username(sub.getUser().getUsername())
                                .accepted(sub.getAccepted())
                                .build())
                        .collect(Collectors.toList()))
                .messages(user.getMessages().stream().map(message ->
                        MessageDto.builder()
                                .id(message.getId())
                                .senderUsername(message.getSender().getUsername())
                                .recipientUsername(message.getRecipient().getUsername())
                                .text(message.getText())
                                .sendAt(message.getSendAt())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }

    public UserEntity mapAuthRequestToUserEntity(AuthRegisterRequest request) {
        return UserEntity.builder()
                .email(request.getEmail())
                .username(request.getUsername())
                .password(request.getPassword())
                .posts(new HashSet<>())
                .messages(new HashSet<>())
                .roles(new HashSet<>())
                .subscribers(new HashSet<>())
                .subscriptions(new HashSet<>())
                .build();
    }

}
