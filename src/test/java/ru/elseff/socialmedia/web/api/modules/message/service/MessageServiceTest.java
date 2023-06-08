package ru.elseff.socialmedia.web.api.modules.message.service;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.elseff.socialmedia.persistense.MessageEntity;
import ru.elseff.socialmedia.persistense.UserEntity;
import ru.elseff.socialmedia.persistense.dao.MessageRepository;
import ru.elseff.socialmedia.web.api.modules.message.dto.SendMessageDto;
import ru.elseff.socialmedia.web.api.modules.user.service.UserService;

import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@FieldDefaults(level = AccessLevel.PRIVATE)
class MessageServiceTest {

    @InjectMocks
    MessageService messageService;

    @Mock
    UserService userService;

    @Mock
    MessageRepository messageRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Найти все сообщения пользователя")
    void findAllByRecipient() {
        given(userService.getCurrentAuthUser()).willReturn(getUserEntity());
        given(messageRepository.findAllByRecipient(any(UserEntity.class))).willReturn(Set.of(getMessage1(), getMessage2()));

        Set<MessageEntity> expectedMessages = Set.of(getMessage1(), getMessage2());

        Set<MessageEntity> actualMessages = messageRepository.findAllByRecipient(userService.getCurrentAuthUser());

        Assertions.assertEquals(expectedMessages, actualMessages);
        verify(userService, times(1)).getCurrentAuthUser();
        verify(messageRepository, times(1)).findAllByRecipient(any(UserEntity.class));
        verifyNoMoreInteractions(userService);
        verifyNoMoreInteractions(messageRepository);
    }

    @Test
    @DisplayName("Найти все сообщения от конкретного пользователя")
    void findAllBySenderUsername() {
        given(userService.findByUsername(anyString())).willReturn(java.util.Optional.ofNullable(getUserEntity()));
        given(messageRepository.findAllBySender(any(UserEntity.class))).willReturn(Set.of(getMessage1(), getMessage2()));

        Set<MessageEntity> expectedMessages = Set.of(getMessage1(), getMessage2());

        Set<MessageEntity> actualMessages = messageService.findAllBySenderUsername(getUserEntity().getUsername());

        Assertions.assertEquals(expectedMessages, actualMessages);
        verify(userService, times(1)).findByUsername(anyString());
        verify(messageRepository, times(1)).findAllBySender(any(UserEntity.class));
        verifyNoMoreInteractions(userService);
        verifyNoMoreInteractions(messageRepository);
    }

    @Test
    @DisplayName("Найти все сообщения от конкретного пользователя, если он не найден")
    void findAllBySenderUsername_If_Sender_Is_Not_Found() {
        given(userService.findByUsername(anyString())).willReturn(Optional.empty());

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
                () -> messageService.findAllBySenderUsername(anyString()));

        String expectedExceptionMessage = "user not found";
        String actualExceptionMessage = exception.getMessage();

        Assertions.assertEquals(expectedExceptionMessage, actualExceptionMessage);
        verify(userService, times(1)).findByUsername(anyString());
        verifyNoMoreInteractions(userService);
        verifyNoInteractions(messageRepository);
    }

    @Test
    @DisplayName("Отправить сообщение пользователю по имени")
    void sendMessageToUserByUsername() {
        given(userService.findByUsername(anyString())).willReturn(Optional.ofNullable(getUserEntity()));
        given(userService.getCurrentAuthUser()).willReturn(getUserEntity());
        given(messageRepository.save(any(MessageEntity.class))).willReturn(getMessageEntityFromDb());

        MessageEntity expectedMessage = getMessageEntityFromDb();

        MessageEntity actualMessage = messageService.sendMessageToUserByUsername(getSendMessageDto(), getUserEntity().getUsername());

        Assertions.assertEquals(expectedMessage, actualMessage);
        verify(userService, times(1)).findByUsername(anyString());
        verify(userService, times(1)).getCurrentAuthUser();
        verify(messageRepository, times(1)).save(any(MessageEntity.class));
        verifyNoMoreInteractions(userService);
        verifyNoMoreInteractions(messageRepository);
    }

    @Test
    @DisplayName("Отправить сообщение несуществующему пользователю")
    void sendMessageToUserByUsername_If_User_Is_Not_Found() {
        given(userService.findByUsername(anyString())).willReturn(Optional.empty());

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
                () -> messageService.sendMessageToUserByUsername(getSendMessageDto(), "not found"));

        String expectedExceptionMessage = "user not found";

        String actualExceptionMessage = exception.getMessage();

        Assertions.assertEquals(expectedExceptionMessage, actualExceptionMessage);
        verify(userService, times(1)).findByUsername(anyString());
        verifyNoMoreInteractions(userService);
        verifyNoInteractions(messageRepository);
    }


    private SendMessageDto getSendMessageDto() {
        return SendMessageDto.builder()
                .text(getMessage1().getText())
                .build();
    }

    private MessageEntity getMessageEntityFromDb() {
        return MessageEntity.builder()
                .id(1L)
                .text(getMessage1().getText())
                .recipient(getUserEntity())
                .sender(getUserEntity())
                .build();

    }

    private UserEntity getUserEntity() {
        return UserEntity.builder()
                .email("test@test.com")
                .username("testusername")
                .password("testpassword")
                .build();
    }

    private MessageEntity getMessage1() {
        return MessageEntity.builder()
                .text("test message")
                .build();
    }

    private MessageEntity getMessage2() {
        return MessageEntity.builder()
                .text("test message 2")
                .build();
    }
}