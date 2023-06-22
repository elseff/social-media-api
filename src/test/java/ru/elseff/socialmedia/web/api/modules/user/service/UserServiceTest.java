package ru.elseff.socialmedia.web.api.modules.user.service;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import ru.elseff.socialmedia.persistense.SubscriptionEntity;
import ru.elseff.socialmedia.persistense.SubscriptionID;
import ru.elseff.socialmedia.persistense.UserEntity;
import ru.elseff.socialmedia.persistense.dao.SubscriptionRepository;
import ru.elseff.socialmedia.persistense.dao.UserRepository;
import ru.elseff.socialmedia.security.UserDetailsImpl;

import java.util.List;
import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@FieldDefaults(level = AccessLevel.PRIVATE)
class UserServiceTest {

    @InjectMocks
    UserService userService;

    @Mock
    UserRepository userRepository;

    @Mock
    SubscriptionRepository subscriptionRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Найти всех пользователей")
    void findAll() {
        given(userRepository.findAll()).willReturn(List.of(getUser1(), getUser2()));

        List<UserEntity> expectedUsers = List.of(getUser1(), getUser2());
        List<UserEntity> actualUsers = userService.findAll();

        Assertions.assertEquals(expectedUsers, actualUsers);

        verify(userRepository, times(1)).findAll();
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(subscriptionRepository);
    }

    @Test
    @DisplayName("Найти пользователя по id")
    void findById() {
        given(userRepository.findById(anyLong())).willReturn(java.util.Optional.of(getUser1()));

        UserEntity expectedUser = getUser1();
        UserEntity actualUser = userService.findById(anyLong());

        Assertions.assertEquals(expectedUser, actualUser);

        verify(userRepository, times(1)).findById(anyLong());
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(subscriptionRepository);
    }

    @Test
    @DisplayName("Найти пользователя по id - не найден")
    void findById_If_Not_Found() {
        given(userRepository.findById(anyLong())).willReturn(Optional.empty());

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
                () -> userService.findById(0L));

        String expectedExceptionMessage = "user not found";
        String actualExceptionMessage = exception.getMessage();

        Assertions.assertEquals(expectedExceptionMessage, actualExceptionMessage);

        verify(userRepository, times(1)).findById(anyLong());
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(subscriptionRepository);
    }

    @Test
    @DisplayName("Найти пользователя по username")
    void findByUsername() {
        given(userRepository.findByUsername(anyString())).willReturn(Optional.of(getUser1()));

        UserEntity expectedUser = getUser1();
        UserEntity actualUser = userService.findByUsername(anyString());

        Assertions.assertEquals(expectedUser, actualUser);

        verify(userRepository, times(1)).findByUsername(anyString());
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(subscriptionRepository);
    }

    @Test
    @DisplayName("Найти пользователя по username - не найден")
    void findByUsername_If_Not_Found() {
        given(userRepository.findByUsername(anyString())).willReturn(Optional.empty());

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
                () -> userService.findByUsername(anyString()));

        String expectedExceptionMessage = "user not found";
        String actualExceptionMessage = exception.getMessage();

        Assertions.assertEquals(expectedExceptionMessage, actualExceptionMessage);

        verify(userRepository, times(1)).findByUsername(anyString());
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(subscriptionRepository);
    }

    @Test
    @DisplayName("Получить текущего пользователя")
    void getCurrentAuthUser() {
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);

        given(securityContext.getAuthentication()).willReturn(authentication);
        given(authentication.getPrincipal()).willReturn(getUserDetails());
        given(userRepository.findByUsername(anyString())).willReturn(Optional.of(getUser1()));
        SecurityContextHolder.setContext(securityContext);

        UserEntity expectedUser = getUser1();
        UserEntity actualUser = userService.getCurrentAuthUser();

        Assertions.assertEquals(expectedUser, actualUser);

        verify(securityContext, times(1)).getAuthentication();
        verify(authentication, times(1)).getPrincipal();
        verify(userRepository, times(1)).findByUsername(anyString());
        verifyNoMoreInteractions(securityContext);
        verifyNoMoreInteractions(authentication);
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(subscriptionRepository);
    }

    @Test
    @DisplayName("Получить текущего пользователя - если principal null")
    void getCurrentAuthUser_If_Principal_Is_Null() {
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);

        given(securityContext.getAuthentication()).willReturn(authentication);
        given(authentication.getPrincipal()).willReturn(getUserDetails());
        given(userRepository.findByUsername(anyString())).willReturn(Optional.empty());
        SecurityContextHolder.setContext(securityContext);

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
                () -> userService.getCurrentAuthUser());

        String expectedExceptionMessage = "something wrong";
        String actualExceptionMessage = exception.getMessage();

        Assertions.assertEquals(expectedExceptionMessage, actualExceptionMessage);

        verify(userRepository, times(1)).findByUsername(anyString());
        verify(securityContext, times(1)).getAuthentication();
        verify(authentication, times(1)).getPrincipal();
        verifyNoMoreInteractions(securityContext);
        verifyNoMoreInteractions(authentication);
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(subscriptionRepository);
    }

    @Test
    @DisplayName("Найти друзей по имени пользователя")
    void findFriendsByUsername() {
        given(userRepository.findByUsername(anyString())).willReturn(Optional.of(getUser1()));
        given(subscriptionRepository.findAllByUserAndAccepted(any(UserEntity.class), anyBoolean())).willReturn(
                List.of(getSubscription())
        );

        List<UserEntity> expectedFriends = List.of(getUser2());
        List<UserEntity> actualFriends = userService.findFriendsByUsername(anyString());

        Assertions.assertEquals(expectedFriends, actualFriends);

        verify(userRepository, times(1)).findByUsername(anyString());
        verify(subscriptionRepository, times(1))
                .findAllByUserAndAccepted(any(UserEntity.class), anyBoolean());
        verifyNoMoreInteractions(userRepository);
        verifyNoMoreInteractions(subscriptionRepository);
    }

    @Test
    @DisplayName("Найти друзей по имени пользователя - пользователь не найден")
    void findFriendsByUsername_If_User_Not_Found() {
        given(userRepository.findByUsername(anyString())).willReturn(Optional.empty());

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
                () -> userService.findFriendsByUsername(anyString()));

        String expectedExceptionMessage = "user not found";
        String actualExceptionMessage = exception.getMessage();

        Assertions.assertEquals(expectedExceptionMessage, actualExceptionMessage);

        verify(userRepository, times(1)).findByUsername(anyString());
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(subscriptionRepository);
    }

    private SubscriptionEntity getSubscription() {
        return SubscriptionEntity.builder()
                .id(SubscriptionID.builder()
                        .subscriberId(getUser2().getId())
                        .userId(getUser1().getId())
                        .build())
                .user(getUser1())
                .subscriber(getUser2())
                .accepted(true)
                .build();
    }

    private UserDetailsImpl getUserDetails() {
        return UserDetailsImpl.toUserDetails(getUser1());
    }

    private UserEntity getUser1() {
        return UserEntity.builder()
                .id(1L)
                .username("test_username1")
                .email("test@test.com")
                .build();
    }

    private UserEntity getUser2() {
        return UserEntity.builder()
                .id(2L)
                .username("test_username2")
                .email("test2@test.com")
                .build();
    }
}