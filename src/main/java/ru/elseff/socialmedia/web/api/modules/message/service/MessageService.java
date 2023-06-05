package ru.elseff.socialmedia.web.api.modules.message.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import ru.elseff.socialmedia.persistense.MessageEntity;
import ru.elseff.socialmedia.persistense.UserEntity;
import ru.elseff.socialmedia.persistense.dao.MessageRepository;
import ru.elseff.socialmedia.web.api.modules.message.dto.SendMessageDto;
import ru.elseff.socialmedia.web.api.modules.user.service.UserService;

import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Set;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MessageService {

    UserService userService;

    MessageRepository messageRepository;

    @Transactional
    public Set<MessageEntity> findAllByRecipient(UserEntity recipient) {
        return messageRepository.findAllByRecipient(recipient);
    }

    @Transactional
    public Set<MessageEntity> findAllBySenderUsername(String senderUsername) {
        UserEntity sender = userService.findByUsername(senderUsername)
                .orElseThrow(() -> new IllegalArgumentException("user not found"));

        return messageRepository.findAllBySender(sender);
    }

    @Transactional
    public MessageEntity sendMessageToUserByUsername(SendMessageDto messageDto, String username) {
        UserEntity recipient = userService.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("user not found"));

        MessageEntity message = MessageEntity.builder()
                .sendAt(Timestamp.from(Instant.now()))
                .sender(userService.getCurrentAuthUser())
                .recipient(recipient)
                .text(messageDto.getText())
                .build();

        return messageRepository.save(message);
    }
}
