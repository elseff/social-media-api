package ru.elseff.socialmedia.web.api.modules.message.controller;

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
import ru.elseff.socialmedia.exception.handling.dto.Violation;
import ru.elseff.socialmedia.persistense.UserEntity;
import ru.elseff.socialmedia.persistense.dao.UserRepository;
import ru.elseff.socialmedia.web.api.modules.message.dto.MessageDto;
import ru.elseff.socialmedia.web.api.modules.message.dto.SendMessageDto;
import ru.elseff.socialmedia.web.api.modules.message.dto.mapper.MessageDtoMapper;
import ru.elseff.socialmedia.web.api.modules.message.service.MessageService;
import ru.elseff.socialmedia.web.api.modules.user.service.UserService;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@FieldDefaults(level = AccessLevel.PRIVATE)
class MessageControllerTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest");

    @Autowired
    MessageDtoMapper messageDtoMapper;

    @Autowired
    MessageService messageService;

    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;


    final String endpoint = "/api/v1/messages";

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        userRepository.save(getFirstUserEntity());
        userRepository.save(getSecondUserEntity());
    }

    @Test
    @DisplayName("Context loads")
    void contextLoads() {
        Assertions.assertNotNull(messageDtoMapper);
        Assertions.assertNotNull(messageService);
        Assertions.assertNotNull(userService);
        Assertions.assertNotNull(mockMvc);
        Assertions.assertNotNull(objectMapper);
    }

    @Test
    @DisplayName("Найти все сообщения")
    @WithUserDetails(value = "testusername2@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void findAllMessages() throws Exception {
        //send messages to yourself
        messageService.sendMessageToUserByUsername(getFirstMessageDto(), getSecondUserEntity().getUsername());
        messageService.sendMessageToUserByUsername(getSecondMessageDto(), getSecondUserEntity().getUsername());

        MockHttpServletRequestBuilder request = get(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8);

        String response = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        List<MessageDto> messages = objectMapper.readValue(response, new TypeReference<>() {
        });

        List<String> expectedTextsMessages = new ArrayList<>(List.of(
                "test first message",
                "test second message"
        ));
        expectedTextsMessages.sort(Comparator.naturalOrder());

        List<String> actualTextsMessages = messages.
                stream()
                .map(MessageDto::getText)
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());

        Assertions.assertEquals(expectedTextsMessages, actualTextsMessages);
    }

    @Test
    @DisplayName("Найти все сообщения от конкретного пользователя")
    @WithUserDetails(value = "testusername2@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void findAllBySender() throws Exception {
        //send messages to yourself
        messageService.sendMessageToUserByUsername(getFirstMessageDto(), getSecondUserEntity().getUsername());
        messageService.sendMessageToUserByUsername(getSecondMessageDto(), getSecondUserEntity().getUsername());

        String endpoint = this.endpoint + "/" + getSecondUserEntity().getUsername();

        MockHttpServletRequestBuilder request = get(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8);

        String response = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        List<MessageDto> messages = objectMapper.readValue(response, new TypeReference<>() {
        });

        List<String> expectedTextsMessages = new ArrayList<>(List.of(
                "test first message",
                "test second message"
        ));
        expectedTextsMessages.sort(Comparator.naturalOrder());

        List<String> actualTextsMessages = messages.
                stream()
                .map(MessageDto::getText)
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());

        Assertions.assertEquals(expectedTextsMessages, actualTextsMessages);
    }

    @Test
    @DisplayName("Найти сообщения от конкретног пользователя, если он не найден")
    @WithUserDetails(value = "testusername2@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void findAllMessages_If_User_Is_Not_Found() throws Exception {
        String endpoint = this.endpoint + "/notfound";
        MockHttpServletRequestBuilder request = get(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8);

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Отправить сообщение")
    @WithUserDetails(value = "testusername2@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void sendMessage() throws Exception {
        String firstMessageAsString = objectMapper.writeValueAsString(getFirstMessageDto());

        String endpoint = this.endpoint + "/send/" + getSecondUserEntity().getUsername();

        MockHttpServletRequestBuilder request = post(endpoint)
                .content(firstMessageAsString)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8);

        String response = mockMvc.perform(request)
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        String expectedMessageText = "test first message";
        String expectedSenderUsername = getSecondUserEntity().getUsername();

        MessageDto message = objectMapper.readValue(response, MessageDto.class);

        String actualMessageText = message.getText();
        String actualSenderUsername = message.getSenderUsername();

        Assertions.assertEquals(expectedMessageText, actualMessageText);
        Assertions.assertEquals(expectedSenderUsername, actualSenderUsername);
    }

    @Test
    @DisplayName("Отправить сообщение, если пользователь не найден")
    @WithUserDetails(value = "testusername2@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void sendMessage_If_User_Is_Not_Found() throws Exception {
        String endPoint = this.endpoint + "/send/notfound";

        MockHttpServletRequestBuilder request = post(endPoint)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8);

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Отправить сообщение, если оно невалидно")
    @WithUserDetails(value = "testusername2@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void sendMessage_If_MessageDto_Is_Not_Valid() throws Exception {
        String notValidMessage = objectMapper.writeValueAsString(getNotValidMessageDto());

        String endpoint = this.endpoint + "/send/" + getSecondUserEntity().getUsername();

        MockHttpServletRequestBuilder request = post(endpoint)
                .content(notValidMessage)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8);

        String response = mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        String stringList = response.substring(14);
        List<Violation> violations = objectMapper.readValue(stringList, new TypeReference<>() {
        });

        List<String> expectedViolations = new ArrayList<>(List.of(
                "Поле текст должно содержать символы"
        ));

        List<String> actualViolations = violations.stream().map(Violation::getMessage).collect(Collectors.toList());

        Assertions.assertEquals(expectedViolations, actualViolations);
    }

    private SendMessageDto getNotValidMessageDto() {
        return SendMessageDto.builder()
                .text("")
                .build();
    }

    private SendMessageDto getFirstMessageDto() {
        return SendMessageDto.builder()
                .text("test first message")
                .build();
    }

    private SendMessageDto getSecondMessageDto() {
        return SendMessageDto.builder()
                .text("test second message")
                .build();
    }

    private UserEntity getFirstUserEntity() {
        return UserEntity.builder()
                .username("test_username_1")
                .email("testusername1@gmail.com")
                .password("test_password_1")
                .build();
    }

    private UserEntity getSecondUserEntity() {
        return UserEntity.builder()
                .username("test_username_2")
                .email("testusername2@gmail.com")
                .password("test_password_2")
                .build();
    }
}
