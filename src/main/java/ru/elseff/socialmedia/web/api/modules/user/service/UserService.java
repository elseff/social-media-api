package ru.elseff.socialmedia.web.api.modules.user.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import ru.elseff.socialmedia.persistense.SubscriptionEntity;
import ru.elseff.socialmedia.persistense.UserEntity;
import ru.elseff.socialmedia.persistense.dao.SubscriptionRepository;
import ru.elseff.socialmedia.persistense.dao.UserRepository;
import ru.elseff.socialmedia.security.UserDetailsImpl;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {

    UserRepository userRepository;

    SubscriptionRepository subscriptionRepository;

    @Transactional
    public List<UserEntity> findAll() {
        return userRepository.findAll();
    }

    @Transactional
    public Optional<UserEntity> findById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional
    public Optional<UserEntity> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional
    public UserEntity getCurrentAuthUser() {
        UserDetailsImpl principal = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return userRepository.findByUsername(principal.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("something wrong"));
    }

    @Transactional
    public List<UserEntity> findFriendsByUsername(String username) {
        UserEntity user = findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("user not found"));

        List<SubscriptionEntity> friends = subscriptionRepository.findAllByUserAndAccepted(user, true);

        return friends
                .stream()
                .map(SubscriptionEntity::getSubscriber)
                .collect(Collectors.toList());
    }
}
