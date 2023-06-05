package ru.elseff.socialmedia.web.api.modules.auth.controller;

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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.elseff.socialmedia.exception.handling.dto.Violation;
import ru.elseff.socialmedia.persistense.UserEntity;
import ru.elseff.socialmedia.persistense.dao.UserRepository;
import ru.elseff.socialmedia.security.JwtProvider;
import ru.elseff.socialmedia.web.api.modules.auth.dto.AuthLoginRequest;
import ru.elseff.socialmedia.web.api.modules.auth.dto.AuthRegisterRequest;
import ru.elseff.socialmedia.web.api.modules.auth.dto.AuthResponse;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@FieldDefaults(level = AccessLevel.PRIVATE)
class AuthControllerTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest");

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    JwtProvider jwtProvider;

    @Autowired
    MockMvc mockMvc;

    final String endPoint = "/api/v1/auth";

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @BeforeEach
    void setUp() {
        //clear all users
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Context loads")
    public void contextLoads() {
        Assertions.assertNotNull(passwordEncoder);
        Assertions.assertNotNull(userRepository);
        Assertions.assertNotNull(objectMapper);
        Assertions.assertNotNull(jwtProvider);
        Assertions.assertNotNull(mockMvc);
    }

    @Test
    @DisplayName("Регистрация")
    void register() throws Exception {
        AuthRegisterRequest registerRequest = getAuthRegisterRequest();

        String requestBody = objectMapper.writeValueAsString(registerRequest);

        String endPoint = this.endPoint + "/register";

        MockHttpServletRequestBuilder request = post(endPoint)
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8);

        mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
    }

    @Test
    @DisplayName("Регистрация, если такой пользователь уже есть")
    void register_If_Email_Is_Already_Registered() throws Exception {
        userRepository.save(getUserEntity());

        AuthRegisterRequest registerRequest = getAuthRegisterRequest();
        String contentUser = objectMapper.writeValueAsString(registerRequest);

        String endPoint = this.endPoint + "/register";

        MockHttpServletRequestBuilder request = post(endPoint)
                .content(contentUser)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8);

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Регистрация, если неверные пользовательские данные")
    void register_If_User_Is_Not_Valid() throws Exception {
        AuthRegisterRequest registerRequest = getNotValidAuthRegisterRequest();
        String contentUser = objectMapper.writeValueAsString(registerRequest);

        String endPoint = this.endPoint + "/register";

        MockHttpServletRequestBuilder request = post(endPoint)
                .content(contentUser)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8);

        String response = mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        //удаляем первые 14 символов, чтобы получить список в чистом виде
        String stringList = response.substring(14);
        List<Violation> violations = objectMapper.readValue(stringList, new TypeReference<>() {
        });

        List<String> expectedViolations = new ArrayList<>(List.of(
                "Длина пароля должна превышать 4 символа",
                "Поле Электронная почта должна быть действительной",
                "Размер имени должен быть не меньше 5 и не больше 40 символов"
        ));
        expectedViolations.sort(Comparator.naturalOrder());

        List<String> actualViolations = violations.stream()
                .map(Violation::getMessage)
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());

        Assertions.assertEquals(expectedViolations, actualViolations);
    }

    @Test
    @DisplayName("Вход")
    void login() throws Exception {
        userRepository.save(getUserEntity());

        AuthLoginRequest authLoginRequest = getValidAuthRequest();
        String contentAuthRequest = objectMapper.writeValueAsString(authLoginRequest);

        String endPoint = this.endPoint + "/login";

        MockHttpServletRequestBuilder request = post(endPoint)
                .content(contentAuthRequest)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8);

        String response = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        AuthResponse authResponse = objectMapper.readValue(response, AuthResponse.class);

        String expectedAuthToken = jwtProvider.generateToken(authLoginRequest.getEmail());
        String actualAuthToken = authResponse.getToken();

        Assertions.assertEquals(expectedAuthToken, actualAuthToken);
    }

    @Test
    @DisplayName("Вход, если пользователь не найден")
    void login_If_User_Is_Not_Found() throws Exception {
        AuthLoginRequest authLoginRequest = getValidAuthRequest();
        String contentAuthRequest = objectMapper.writeValueAsString(authLoginRequest);

        String endPoint = this.endPoint + "/login";

        MockHttpServletRequestBuilder request = post(endPoint)
                .content(contentAuthRequest)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8);

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Вход, если неверный пароль")
    void login_If_Password_Is_Incorrect() throws Exception {
        UserEntity userEntity = getUserEntity();
        userEntity.setPassword(passwordEncoder.encode("test"));
        userRepository.save(userEntity);

        AuthLoginRequest authLoginRequest = getValidAuthRequest();
        String contentAuthRequest = objectMapper.writeValueAsString(authLoginRequest);

        String endPoint = this.endPoint + "/login";

        MockHttpServletRequestBuilder request = post(endPoint)
                .content(contentAuthRequest)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8);

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Вход, если неверные пользовательские данные")
    void login_If_AuthRequest_Is_Not_Valid() throws Exception {
        UserEntity userEntity = userRepository.save(getUserEntity());

        AuthLoginRequest authLoginRequest = getNotValidAuthRequest();
        String contentAuthRequest = objectMapper.writeValueAsString(authLoginRequest);

        String endPoint = this.endPoint + "/login";

        MockHttpServletRequestBuilder request = post(endPoint)
                .content(contentAuthRequest)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8);

        String response = mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        //удаляем первые 14 символов, чтобы получить список в чистом виде
        String stringList = response.substring(14);
        List<Violation> violations = objectMapper.readValue(stringList, new TypeReference<>() {
        });

        List<String> expectedViolations = new ArrayList<>(List.of(
                "Электронная почта должна быть действительной",
                "Длина пароля должна быть не менее 4 символов"
        ));
        expectedViolations.sort(Comparator.naturalOrder());

        List<String> actualViolations = violations.stream()
                .map(Violation::getMessage)
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());

        Assertions.assertEquals(expectedViolations, actualViolations);
    }

    private AuthRegisterRequest getAuthRegisterRequest() {
        return AuthRegisterRequest.builder()
                .username("testtt")
                .email("test@test.com")
                .password("test")
                .build();
    }

    private UserEntity getUserEntity() {
        return UserEntity.builder()
                .username("test")
                .email("test@test.com")
                .password(passwordEncoder.encode("root"))
                .build();
    }

    private AuthRegisterRequest getNotValidAuthRegisterRequest() {
        return AuthRegisterRequest.builder()
                .username("test")
                .email("test")
                .password("t")
                .build();
    }

    private AuthLoginRequest getValidAuthRequest() {
        return AuthLoginRequest.builder()
                .email("test@test.com")
                .password("root")
                .build();
    }

    private AuthLoginRequest getNotValidAuthRequest() {
        return AuthLoginRequest.builder()
                .email("t")
                .password("t")
                .build();
    }
}