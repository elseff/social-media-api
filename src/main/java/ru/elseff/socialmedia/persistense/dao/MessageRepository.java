package ru.elseff.socialmedia.persistense.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.elseff.socialmedia.persistense.MessageEntity;
import ru.elseff.socialmedia.persistense.UserEntity;

import java.util.Set;

@Repository
public interface MessageRepository extends JpaRepository<MessageEntity, Long> {

    Set<MessageEntity> findAllByRecipient(UserEntity recipient);

    Set<MessageEntity> findAllBySender(UserEntity sender);
}
