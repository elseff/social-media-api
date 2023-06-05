package ru.elseff.socialmedia.web.api.modules.message.dto.mapper;

import org.springframework.stereotype.Component;
import ru.elseff.socialmedia.persistense.MessageEntity;
import ru.elseff.socialmedia.web.api.modules.message.dto.MessageDto;

@Component
public class MessageDtoMapper {

    public MessageDto mapMessageEntityToDto(MessageEntity messageEntity) {
        return MessageDto.builder()
                .id(messageEntity.getId())
                .text(messageEntity.getText())
                .sendAt(messageEntity.getSendAt())
                .recipientUsername(messageEntity.getRecipient().getUsername())
                .senderUsername(messageEntity.getSender().getUsername())
                .build();
    }
}
