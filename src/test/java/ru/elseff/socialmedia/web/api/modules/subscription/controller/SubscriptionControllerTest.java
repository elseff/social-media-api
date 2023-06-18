package ru.elseff.socialmedia.web.api.modules.subscription.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
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
import ru.elseff.socialmedia.web.api.modules.subscription.service.SubscriptionService;

import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@FieldDefaults(level = AccessLevel.PRIVATE)
class SubscriptionControllerTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest");

    @Autowired
    SubscriptionRepository subscriptionRepository;

    @Autowired
    SubscriptionService subscriptionService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    MockMvc mockMvc;

    final String endpoint = "/api/v1/subscriptions/change-subscription";

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        userRepository.save(getUser1());
        userRepository.save(getUser2());
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Test
    @DisplayName("Context loads")
    void contextLoads() {
        Assertions.assertNotNull(mockMvc);
        Assertions.assertNotNull(objectMapper);
        Assertions.assertNotNull(subscriptionService);
    }

    @Test
    @DisplayName("Изменить статус подписк - подписаться на нового пользователя")
    @WithUserDetails(value = "test@test.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void changeSub_New_Subscription() throws Exception {
        String endpoint = this.endpoint + "/" + getUser2().getUsername();
        MockHttpServletRequestBuilder request = post(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8);

        String response = mockMvc.perform(request)
                .andExpect(status().isAccepted())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        String expectedSubscriptionStatus = "you subscribe " + getUser2().getUsername() + " now";
        String actualSubscriptionStatus = JsonPath.read(response, "$['subscriptionStatus']");

        Assertions.assertEquals(expectedSubscriptionStatus, actualSubscriptionStatus);
    }

    @Test
    @DisplayName("Изменить статус подписки - подписаться на себя")
    @WithUserDetails(value = "test@test.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void changeSub_Subscribe_To_Yourself() throws Exception {
        String endpoint = this.endpoint + "/" + getUser1().getUsername();

        MockHttpServletRequestBuilder request = post(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8);

        String response = mockMvc.perform(request)
                .andExpect(status().isAccepted())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        String expectedSubscriptionStatus = "You can't subscribe yourself";
        String actualSubscriptionStatus = JsonPath.read(response, "$['subscriptionStatus']");

        Assertions.assertEquals(expectedSubscriptionStatus, actualSubscriptionStatus);
    }

    @Test
    @DisplayName("Изменить статус подписки, если пользователь не найден")
    @WithUserDetails(value = "test@test.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void changeSub_If_User_Is_Not_Found() throws Exception {
        String endpoint = this.endpoint + "/notfound";

        MockHttpServletRequestBuilder request = post(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8);

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Изменить статус подписки - отменить подписку")
    @WithUserDetails(value = "test@test.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void changeSub_Cancel_Subscription() throws Exception {
        subscriptionService.changeSub(getUser2().getUsername());
        String endpoint = this.endpoint + "/" + getUser2().getUsername();

        MockHttpServletRequestBuilder request = post(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8);

        String response = mockMvc.perform(request)
                .andExpect(status().isAccepted())
                .andReturn().getResponse().getContentAsString();

        String expectedSubscriptionStatus = "subscription on " + getUser2().getUsername() + " canceled";
        String actualSubscriptionStatus = JsonPath.read(response, "$['subscriptionStatus']");

        Assertions.assertEquals(expectedSubscriptionStatus, actualSubscriptionStatus);
    }

    @Test
    @DisplayName("Изменить статус подписки - принять запрос на подписку(подписаться в ответ)")
    @WithUserDetails(value = "test@test.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void changeSub_Accept_Subscription() throws Exception {
        UserEntity user = userRepository.findByUsername(getUser1().getUsername()).get();
        UserEntity subscriber = userRepository.findByUsername(getUser2().getUsername()).get();
        SubscriptionEntity entity = getSubscriptionEntity(user, subscriber);
        subscriptionRepository.save(entity);
        String endpoint = this.endpoint + "/" + getUser2().getUsername();

        MockHttpServletRequestBuilder request = post(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8);

        String response = mockMvc.perform(request)
                .andExpect(status().isAccepted())
                .andReturn().getResponse().getContentAsString();

        String expectedSubscriptionStatus = "subscription of " + getUser2().getUsername() + " accepted";
        String actualSubscriptionStatus = JsonPath.read(response, "$['subscriptionStatus']");

        Assertions.assertEquals(expectedSubscriptionStatus, actualSubscriptionStatus);
    }

    private SubscriptionEntity getSubscriptionEntity(UserEntity user, UserEntity subscriber) {
        return SubscriptionEntity.builder()
                .id(SubscriptionID.builder()
                        .subscriberId(subscriber.getId())
                        .userId(user.getId())
                        .build())
                .subscriber(subscriber)
                .user(user)
                .accepted(false)
                .build();
    }

    private UserEntity getUser1() {
        return UserEntity.builder()
                .email("test@test.com")
                .username("test_username")
                .password("test_password")
                .build();
    }

    private UserEntity getUser2() {
        return UserEntity.builder()
                .email("test2@test.com")
                .username("test_username2")
                .password("test_password2")
                .build();
    }
}