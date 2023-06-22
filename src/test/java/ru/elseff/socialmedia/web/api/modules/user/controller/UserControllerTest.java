package ru.elseff.socialmedia.web.api.modules.user.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.elseff.socialmedia.persistense.SubscriptionEntity;
import ru.elseff.socialmedia.persistense.SubscriptionID;
import ru.elseff.socialmedia.persistense.UserEntity;
import ru.elseff.socialmedia.persistense.dao.SubscriptionRepository;
import ru.elseff.socialmedia.persistense.dao.UserRepository;
import ru.elseff.socialmedia.web.api.modules.user.dto.UserDto;
import ru.elseff.socialmedia.web.api.modules.user.dto.mapper.UserDtoMapper;

import java.nio.charset.StandardCharsets;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@FieldDefaults(level = AccessLevel.PRIVATE)
class UserControllerTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest");

    @Autowired
    SubscriptionRepository subscriptionRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserDtoMapper userDtoMapper;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    MockMvc mockMvc;

    static final String USER_DETAILS_EMAIL = "test@test.com";

    final String endpoint = "/api/v1/users";

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
    }

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        userRepository.save(getUser1());
        userRepository.save(getUser2());
    }

    @Test
    @DisplayName("Context loads")
    void contextLoads() {
        Assertions.assertNotNull(subscriptionRepository);
        Assertions.assertNotNull(userRepository);
        Assertions.assertNotNull(userDtoMapper);
        Assertions.assertNotNull(objectMapper);
        Assertions.assertNotNull(mockMvc);
    }

    @Test
    @DisplayName("Получить текущего пользователя")
    @WithUserDetails(value = USER_DETAILS_EMAIL, setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void getMe() throws Exception {
        String endpoint = this.endpoint + "/me";

        MockHttpServletRequestBuilder request = get(endpoint)
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON);

        String response = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        UserEntity user = objectMapper.readValue(response, UserEntity.class);

        String expectedUsername = getUser1().getUsername();
        String actualUsername = user.getUsername();

        Assertions.assertEquals(expectedUsername, actualUsername);
    }

    @Test
    @DisplayName("Найти друзей")
    @WithUserDetails(value = USER_DETAILS_EMAIL, setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void findFriends() throws Exception {
        UserEntity user = userRepository.findByEmail(getUser1().getEmail()).get();
        UserEntity subscriber = userRepository.findByEmail(getUser2().getEmail()).get();
        subscriptionRepository.save(getSubscriptionEntity(user, subscriber));
        subscriptionRepository.save(getSubscriptionEntity(subscriber, user));

        String endpoint = this.endpoint + "/me/friends";

        MockHttpServletRequestBuilder request = get(endpoint)
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON);

        String response = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        Set<UserDto> actualFriends = objectMapper.readValue(response, new TypeReference<>() {
        });
        String expectedFriendUsername = getUser2().getUsername();
        String actualFriendUsername = actualFriends.iterator().next().getUsername();

        Assertions.assertEquals(expectedFriendUsername, actualFriendUsername);
    }

    private SubscriptionEntity getSubscriptionEntity(UserEntity user, UserEntity subscriber) {
        return SubscriptionEntity.builder()
                .id(SubscriptionID.builder()
                        .subscriberId(subscriber.getId())
                        .userId(user.getId())
                        .build())
                .subscriber(subscriber)
                .user(user)
                .accepted(true)
                .build();
    }

    private UserEntity getUser1() {
        return UserEntity.builder()
                .email(USER_DETAILS_EMAIL)
                .username("test_username")
                .password("test_password")
                .roles(Set.of())
                .posts(Set.of())
                .subscriptions(Set.of())
                .subscribers(Set.of())
                .messages(Set.of())
                .build();
    }

    private UserEntity getUser2() {
        return UserEntity.builder()
                .email("test2@test.com")
                .username("test_username2")
                .password("test_password2")
                .roles(Set.of())
                .posts(Set.of())
                .subscriptions(Set.of())
                .subscribers(Set.of())
                .messages(Set.of())
                .build();
    }
}