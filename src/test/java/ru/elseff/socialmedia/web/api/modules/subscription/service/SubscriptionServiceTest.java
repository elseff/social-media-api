package ru.elseff.socialmedia.web.api.modules.subscription.service;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.elseff.socialmedia.persistense.SubscriptionEntity;
import ru.elseff.socialmedia.persistense.SubscriptionID;
import ru.elseff.socialmedia.persistense.UserEntity;
import ru.elseff.socialmedia.persistense.dao.SubscriptionRepository;
import ru.elseff.socialmedia.web.api.modules.user.service.UserService;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.*;

@FieldDefaults(level = AccessLevel.PRIVATE)
class SubscriptionServiceTest {

    @InjectMocks
    SubscriptionService subscriptionService;

    @Mock
    UserService userService;

    @Mock
    SubscriptionRepository subscriptionRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Изменить статус подписк - подписаться на нового пользователя")
    void changeSub_New_Subscription() {
        given(userService.getCurrentAuthUser()).willReturn(getUser1());
        given(userService.findByUsername(anyString())).willReturn(getUser2());
        given(subscriptionRepository.findByUserAndSubscriber(any(UserEntity.class), any(UserEntity.class))).willReturn(Optional.empty());
        given(subscriptionRepository.save(any(SubscriptionEntity.class))).willReturn(getSubscriptionEntity());

        String expectedSubscriptionStatus = "you subscribe " + getUser2().getUsername() + " now";
        String actualSubscriptionStatus = subscriptionService.changeSub(getUser2().getUsername());

        Assertions.assertEquals(expectedSubscriptionStatus, actualSubscriptionStatus);
        verify(userService, times(1)).getCurrentAuthUser();
        verify(userService, times(1)).findByUsername(anyString());
        verify(subscriptionRepository, times(2)).findByUserAndSubscriber(any(UserEntity.class), any(UserEntity.class));
        verify(subscriptionRepository, times(1)).save(any(SubscriptionEntity.class));
        verifyNoMoreInteractions(userService);
        verifyNoMoreInteractions(subscriptionRepository);
    }

    @Test
    @DisplayName("Изменить статус подписки - подписаться на себя")
    void changeSub_Sub_To_Yourself() {
        given(userService.getCurrentAuthUser()).willReturn(getUser1());
        given(userService.findByUsername(anyString())).willReturn(getUser1());

        String expectedSubscriptionStatus = "You can't subscribe yourself";
        String actualSubscriptionStatus = subscriptionService.changeSub(getUser1().getUsername());

        Assertions.assertEquals(expectedSubscriptionStatus, actualSubscriptionStatus);
        verify(userService, times(1)).getCurrentAuthUser();
        verify(userService, times(1)).findByUsername(anyString());
        verifyNoInteractions(subscriptionRepository);
        verifyNoMoreInteractions(userService);
    }

    @Test
    @DisplayName("Изменить статус подписки, если пользователь не найден")
    void changeSub_If_User_Is_Not_Found() {
        given(userService.findByUsername(anyString())).willThrow(new IllegalArgumentException("user not found"));
        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
                () -> subscriptionService.changeSub(anyString()));

        String exceptedExceptionMessage = "user not found";
        String actualExceptionMessage = exception.getMessage();

        Assertions.assertEquals(exceptedExceptionMessage, actualExceptionMessage);
        verify(userService, times(1)).findByUsername(anyString());
        verifyNoInteractions(subscriptionRepository);
        verifyNoMoreInteractions(userService);
    }

    @Test
    @DisplayName("Изменить статус подписки - отменить подписку")
    void changeSub_Cancel_Subscription() {
        given(subscriptionRepository.findByUserAndSubscriber(getUser2(), getUser1())).willReturn(Optional.of(getSubscriptionEntity()));
        given(subscriptionRepository.findByUserAndSubscriber(getUser1(), getUser2())).willReturn(Optional.empty());
        willDoNothing().given(subscriptionRepository).delete(any(SubscriptionEntity.class));
        given(userService.findByUsername(anyString())).willReturn(getUser2());
        given(userService.getCurrentAuthUser()).willReturn(getUser1());

        String expectedSubscriptionStatus = "subscription on " + getUser2().getUsername() + " canceled";
        String actualSubscriptionStatus = subscriptionService.changeSub(getUser2().getUsername());

        Assertions.assertEquals(expectedSubscriptionStatus, actualSubscriptionStatus);

        verify(subscriptionRepository, times(2)).findByUserAndSubscriber(any(UserEntity.class), any(UserEntity.class));
        verify(subscriptionRepository, times(1)).delete(any(SubscriptionEntity.class));
        verify(userService, times(1)).findByUsername(getUser2().getUsername());
        verify(userService, times(1)).getCurrentAuthUser();
        verifyNoMoreInteractions(subscriptionRepository);
        verifyNoMoreInteractions(userService);
    }

    @Test
    @DisplayName("Изменить статус подписки - удалить друга")
    void changeSub_Remove_Friend() {
        given(subscriptionRepository.findByUserAndSubscriber(getUser2(), getUser1())).willReturn(Optional.of(getSubscriptionEntity()));
        given(subscriptionRepository.findByUserAndSubscriber(getUser1(), getUser2())).willReturn(Optional.of(getInversedSubscriptionEntity()));
        given(subscriptionRepository.save(any(SubscriptionEntity.class))).willReturn(getInversedSubscriptionEntity());
        willDoNothing().given(subscriptionRepository).delete(any(SubscriptionEntity.class));
        given(userService.findByUsername(anyString())).willReturn(getUser2());
        given(userService.getCurrentAuthUser()).willReturn(getUser1());

        String expectedSubscriptionStatus = "subscription on " + getUser2().getUsername() + " canceled";
        String actualSubscriptionStatus = subscriptionService.changeSub(getUser2().getUsername());

        Assertions.assertEquals(expectedSubscriptionStatus, actualSubscriptionStatus);

        verify(subscriptionRepository, times(2)).findByUserAndSubscriber(any(UserEntity.class), any(UserEntity.class));
        verify(subscriptionRepository, times(1)).delete(any(SubscriptionEntity.class));
        verify(subscriptionRepository, times(1)).save(any(SubscriptionEntity.class));
        verify(userService, times(1)).findByUsername(getUser2().getUsername());
        verify(userService, times(1)).getCurrentAuthUser();
        verifyNoMoreInteractions(subscriptionRepository);
        verifyNoMoreInteractions(userService);
    }

    @Test
    @DisplayName("Изменить статус подписки - принять запрос на подписку")
    void changeSub_Accept_Subscription() {
        given(subscriptionRepository.findByUserAndSubscriber(getUser2(), getUser1())).willReturn(Optional.empty());
        given(subscriptionRepository.findByUserAndSubscriber(getUser1(), getUser2())).willReturn(Optional.of(getInversedSubscriptionEntity()));
        given(subscriptionRepository.save(any(SubscriptionEntity.class))).willReturn(getInversedSubscriptionEntity());
        given(userService.findByUsername(anyString())).willReturn(getUser2());
        given(userService.getCurrentAuthUser()).willReturn(getUser1());

        String expectedSubscriptionStatus = "subscription of " + getUser2().getUsername() + " accepted";
        String actualSubscriptionStatus = subscriptionService.changeSub(getUser2().getUsername());

        Assertions.assertEquals(expectedSubscriptionStatus, actualSubscriptionStatus);

        verify(subscriptionRepository, times(2)).findByUserAndSubscriber(any(UserEntity.class), any(UserEntity.class));
        verify(subscriptionRepository, times(1)).save(any(SubscriptionEntity.class));
        verify(userService, times(1)).findByUsername(getUser2().getUsername());
        verify(userService, times(1)).getCurrentAuthUser();
        verifyNoMoreInteractions(subscriptionRepository);
        verifyNoMoreInteractions(userService);
    }

    private SubscriptionEntity getInversedSubscriptionEntity(){
        SubscriptionEntity entity = getSubscriptionEntity();
        entity.setUser(getUser1());
        entity.setSubscriber(getUser2());
        return entity;
    }

    private SubscriptionEntity getSubscriptionEntity() {
        return SubscriptionEntity.builder()
                .id(SubscriptionID.builder()
                        .subscriberId(getUser1().getId())
                        .userId(getUser2().getId())
                        .build())
                .user(getUser2())
                .subscriber(getUser1())
                .build();
    }

    private UserEntity getUser2() {
        return UserEntity.builder()
                .id(2L)
                .email("test2@test.com")
                .username("test2")
                .build();
    }

    private UserEntity getUser1() {
        return UserEntity.builder()
                .id(1L)
                .email("test@test.com")
                .username("test")
                .build();
    }
}