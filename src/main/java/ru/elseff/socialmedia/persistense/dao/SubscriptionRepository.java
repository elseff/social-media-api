package ru.elseff.socialmedia.persistense.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.elseff.socialmedia.persistense.SubscriptionEntity;
import ru.elseff.socialmedia.persistense.SubscriptionID;
import ru.elseff.socialmedia.persistense.UserEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<SubscriptionEntity, SubscriptionID> {
    Optional<SubscriptionEntity> findByUser(UserEntity user);

    Optional<SubscriptionEntity> findByUserAndSubscriber(UserEntity user, UserEntity subscriber);

    List<SubscriptionEntity> findAllByUserAndAccepted(UserEntity user, Boolean accepted);
}
