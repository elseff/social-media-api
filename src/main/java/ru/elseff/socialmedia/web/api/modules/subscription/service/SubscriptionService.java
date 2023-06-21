package ru.elseff.socialmedia.web.api.modules.subscription.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import ru.elseff.socialmedia.persistense.SubscriptionEntity;
import ru.elseff.socialmedia.persistense.SubscriptionID;
import ru.elseff.socialmedia.persistense.UserEntity;
import ru.elseff.socialmedia.persistense.dao.SubscriptionRepository;
import ru.elseff.socialmedia.web.api.modules.user.service.UserService;

import javax.transaction.Transactional;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SubscriptionService {

    SubscriptionRepository subscriptionRepository;

    UserService userService;

    @Transactional
    public String changeSub(String username) {
        UserEntity user = userService.findByUsername(username);

        UserEntity subscriber = userService.getCurrentAuthUser();

        if (subscriber.getUsername().equals(user.getUsername()))
            return "You can't subscribe yourself";

        Optional<SubscriptionEntity> subOptional = subscriptionRepository.findByUserAndSubscriber(user, subscriber);
        if (subOptional.isPresent()) {//если мы подписаны на него, то удаляем нашу подписку и отменяем его, если она есть
            subscriptionRepository.delete(subOptional.get());// удалили нашу подписку
            /*
            если он подписан на нас, статус его подписки - не принята
            */
            Optional<SubscriptionEntity> inversed = subscriptionRepository.findByUserAndSubscriber(subscriber, user);
            inversed.ifPresent(sub -> {
                sub.setAccepted(false);
                subscriptionRepository.save(sub);
            });
            return "subscription on " + username + " canceled";
        } else {// если такой подписки не найдено, то есть либо это новый человек, либо нужно принять подписку
            Optional<SubscriptionEntity> inversed = subscriptionRepository.findByUserAndSubscriber(subscriber, user);
            if (inversed.isPresent()) { //вдруг этот человек подписан на нас
                SubscriptionEntity subscriptionEntity = inversed.get();
                subscriptionEntity.setAccepted(true);
                SubscriptionID id = SubscriptionID.builder()
                        .userId(user.getId())
                        .subscriberId(subscriber.getId())
                        .build();
                SubscriptionEntity subscription = SubscriptionEntity.builder()
                        .id(id)
                        .user(user)
                        .subscriber(subscriber)
                        .accepted(true)
                        .build();
                subscriptionRepository.save(subscription);
                return "subscription of " + username + " accepted";
            } else { //если не подписан на нас, мы подписываемся на него
                SubscriptionID id = SubscriptionID.builder()
                        .userId(user.getId())
                        .subscriberId(subscriber.getId())
                        .build();
                SubscriptionEntity subscription = SubscriptionEntity.builder()
                        .id(id)
                        .subscriber(subscriber)
                        .user(user)
                        .accepted(false)
                        .build();
                subscriptionRepository.save(subscription);
                return "you subscribe " + username + " now";
            }
        }
    }
}