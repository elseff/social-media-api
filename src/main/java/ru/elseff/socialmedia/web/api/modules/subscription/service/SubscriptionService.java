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
        UserEntity user = userService.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("user not found"));

        UserEntity subscriber = userService.getCurrentAuthUser();

        if (subscriber.getUsername().equals(user.getUsername()))
            return "You can't subscribe yourself";

        Optional<SubscriptionEntity> subOptional = subscriptionRepository.findByUser(user);
        if (subOptional.isPresent()) {
            subscriptionRepository.delete(subOptional.get());

            //may be they were friends
            Optional<SubscriptionEntity> inverseSubOptional = subscriptionRepository.findByUser(subscriber);
            if (inverseSubOptional.isPresent()) {
                SubscriptionEntity subscription = inverseSubOptional.get();
                subscription.setAccepted(false);
            }

            return "canceled subscription on " + username;
        } else {
            SubscriptionEntity subscription = SubscriptionEntity.builder()
                    .id(SubscriptionID.builder()
                            .userId(user.getId())
                            .subscriberId(subscriber.getId())
                            .build())
                    .user(user)
                    .subscriber(subscriber)
                    .accepted(false)
                    .build();
            subscriptionRepository.save(subscription);
            return "you subscriber " + username + " now";
        }
    }

    @Transactional
    public String acceptSubscription(String subscriberUsername) {
        UserEntity user = userService.getCurrentAuthUser();

        UserEntity subscriber = userService.findByUsername(subscriberUsername)
                .orElseThrow(() -> new IllegalArgumentException("user not found"));

        SubscriptionID id = SubscriptionID.builder()
                .userId(user.getId())
                .subscriberId(subscriber.getId())
                .build();

        SubscriptionEntity subscription = subscriptionRepository.findById(id).get();
        subscription.setAccepted(true);

        subscriptionRepository.save(subscription);

        SubscriptionEntity inverseSubscription = SubscriptionEntity.builder()
                .id(SubscriptionID.builder()
                        .userId(subscriber.getId())
                        .subscriberId(user.getId())
                        .build())
                .user(subscriber)
                .subscriber(user)
                .accepted(true)
                .build();

        subscriptionRepository.save(inverseSubscription);

        return "accepted subscription for " + subscriberUsername;
    }

}
