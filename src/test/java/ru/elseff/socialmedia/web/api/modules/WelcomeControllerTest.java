package ru.elseff.socialmedia.web.api.modules;


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
import ru.elseff.socialmedia.persistense.UserEntity;
import ru.elseff.socialmedia.persistense.dao.UserRepository;

import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@FieldDefaults(level = AccessLevel.PRIVATE)
class WelcomeControllerTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest");

    @Autowired
    UserRepository userRepository;

    @Autowired
    MockMvc mockMvc;

    final String endpoint = "/welcome";

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Test
    @DisplayName("Context loads")
    void contextLoads() {
        Assertions.assertNotNull(userRepository);
    }

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        userRepository.save(getUser1());
    }


    @Test
    @DisplayName("Приветствие")
    @WithUserDetails(value = "test@test.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void welcome() throws Exception {
        MockHttpServletRequestBuilder request = get(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8);

        String expectedMessage = getUser1().getUsername() + " успешно вошёл и имеет доступ к  API!!!";
        String actualMessage = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        Assertions.assertEquals(expectedMessage, actualMessage);
    }

    @Test
    @DisplayName("Если не вошёл")
    void welcome_If_Not_Login() throws Exception {
        MockHttpServletRequestBuilder request = get(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8);

        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());
    }

    private UserEntity getUser1() {
        return UserEntity.builder()
                .email("test@test.com")
                .username("test_username")
                .password("test_password")
                .build();
    }
}