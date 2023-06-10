package ru.elseff.socialmedia.web.api.modules.post.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PagedResourcesAssembler;
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
import ru.elseff.socialmedia.persistense.PostEntity;
import ru.elseff.socialmedia.persistense.UserEntity;
import ru.elseff.socialmedia.persistense.dao.PostRepository;
import ru.elseff.socialmedia.persistense.dao.UserRepository;
import ru.elseff.socialmedia.web.api.modules.post.dto.PostCreationDto;
import ru.elseff.socialmedia.web.api.modules.post.dto.PostDto;
import ru.elseff.socialmedia.web.api.modules.post.dto.PostUpdateDto;
import ru.elseff.socialmedia.web.api.modules.post.dto.mapper.PostDtoAssembler;
import ru.elseff.socialmedia.web.api.modules.post.service.PostService;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
class PostControllerTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest");

    @Autowired
    PagedResourcesAssembler<PostEntity> pagedResourcesAssembler;

    @Autowired
    PostDtoAssembler postDtoAssembler;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PostRepository postRepository;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    PostService postService;

    @Autowired
    MockMvc mockMvc;

    final String endpoint = "/api/v1/posts";

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
        Assertions.assertNotNull(postService);
        Assertions.assertNotNull(userRepository);
        Assertions.assertNotNull(pagedResourcesAssembler);
    }

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        userRepository.save(getUser1());
        userRepository.save(getUser2());
    }

    @Test
    @DisplayName("Найти все посты")
    @WithUserDetails(value = "test@test.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void findAll() throws Exception {
        postService.addPost(getPost1());
        postService.addPost(getPost2());
        MockHttpServletRequestBuilder request = get(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8);

        String response = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<PostDto> expectedPostList = new ArrayList<>(List.of(getPost1(), getPost2()))
                .stream()
                .map(postDtoAssembler::mapPostEntityToDto).collect(Collectors.toList());

        List<PostDto> actualPostList = new ObjectMapper().readValue(
                JsonPath.parse(response).read("$._embedded.posts").toString(),
                new TypeReference<>() {
                });

        Assertions.assertEquals(expectedPostList, actualPostList);
    }

    @Test
    @DisplayName("Найти пост по id")
    @WithUserDetails(value = "test@test.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void findById() throws Exception {
        PostEntity post = postService.addPost(getPost1());

        String endpoint = this.endpoint + "/" + post.getId();

        MockHttpServletRequestBuilder request = get(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8);

        String response = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        PostEntity expectedPost = postService.findById(post.getId());
        PostDto actualPost = objectMapper.readValue(response, PostDto.class);

        Assertions.assertEquals(expectedPost.getTitle(), actualPost.getTitle());
        Assertions.assertEquals(getUser1().getUsername(), actualPost.getAuthor().getUsername());
    }

    @Test
    @DisplayName("Найти пост, если он не найден")
    @WithUserDetails(value = "test@test.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void findById_If_Post_Is_Not_Found() throws Exception {
        String endpoint = this.endpoint + "/" + 0;

        MockHttpServletRequestBuilder request = get(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8);

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Добавить пост")
    @WithUserDetails(value = "test@test.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void addPost() throws Exception {
        String content = objectMapper.writeValueAsString(getPostCreationDto());

        MockHttpServletRequestBuilder request = post(endpoint)
                .content(content)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8);

        String response = mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        PostDto post = objectMapper.readValue(response, PostDto.class);

        PostEntity postEntity = postService.findById(post.getId());

        Assertions.assertNotNull(postEntity);
    }

    @Test
    @DisplayName("Добавить невалидный пост")
    @WithUserDetails(value = "test@test.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void addPost_If_Is_Invalid() throws Exception {
        String content = objectMapper.writeValueAsString(getInvalidPostCreationDto());

        MockHttpServletRequestBuilder request = post(endpoint)
                .content(content)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8);

        String response = mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        response = response.substring(14);

        List<Violation> expectedViolations = new ArrayList<>(List.of(
                new Violation("title", "Размер названия поста должен быть больше 10 и меньше 100 символов")
        ));
        List<Violation> actualViolations = objectMapper.readValue(response, new TypeReference<>() {
        });

        Assertions.assertEquals(expectedViolations, actualViolations);
    }

    @Test
    @DisplayName("Удалить пост")
    @WithUserDetails(value = "test@test.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void deletePost() throws Exception {
        PostEntity post = postService.addPost(getPost1());

        String endpoint = this.endpoint + "/" + post.getId();

        MockHttpServletRequestBuilder request = delete(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8);

        mockMvc.perform(request)
                .andExpect(status().isNoContent());

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
                () -> postService.findById(post.getId()));

        String expectedExceptionMessage = "post not found";
        String actualExceptionMessage = exception.getMessage();

        Assertions.assertEquals(expectedExceptionMessage, actualExceptionMessage);
    }

    @Test
    @DisplayName("Удалить пост, если он не найден")
    @WithUserDetails(value = "test@test.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void deletePost_If_Not_Found() throws Exception {
        String endpoint = this.endpoint + "/" + 0;

        MockHttpServletRequestBuilder request = delete(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8);

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Удалить чужой пост")
    @WithUserDetails(value = "test2@test.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void deletePost_If_Someone_Else() throws Exception {
        PostEntity savePost = getPost1();
        savePost.setUser(userRepository.getByEmail("test@test.com"));
        PostEntity post = postRepository.save(savePost);

        String endpoint = this.endpoint + "/" + post.getId();

        MockHttpServletRequestBuilder request = delete(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8);

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Обновить пост")
    @WithUserDetails(value = "test@test.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void updatePost() throws Exception {
        PostEntity post = postService.addPost(getPost1());
        String content = objectMapper.writeValueAsString(getPostUpdateDto());

        String endpoint = this.endpoint + "/" + post.getId();

        MockHttpServletRequestBuilder request = patch(endpoint)
                .content(content)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8);

        String response = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        PostDto postDto = objectMapper.readValue(response, PostDto.class);

        String expectedUpdatedText = getPostUpdateDto().getText();
        String actualUpdatedText = postDto.getText();

        Assertions.assertEquals(expectedUpdatedText, actualUpdatedText);
    }

    @Test
    @DisplayName("Обновить пост, если он найден")
    @WithUserDetails(value = "test@test.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void updatePost_If_Is_Not_Found() throws Exception {
        String endpoint = this.endpoint + "/" + 0;

        MockHttpServletRequestBuilder request = delete(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8);

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Обновить пост, если пост для обновления невалидный")
    @WithUserDetails(value = "test@test.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void updatePost_If_Post_Invalid() throws Exception {
        PostEntity post = postService.addPost(getPost1());
        String content = objectMapper.writeValueAsString(getInvalidPostUpdateDto());

        String endpoint = this.endpoint + "/" + post.getId();

        MockHttpServletRequestBuilder request = patch(endpoint)
                .content(content)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8);

        String response = mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        response = response.substring(14);

        List<Violation> expectedViolations = new ArrayList<>(List.of(
                new Violation("title", "Размер названия поста должен быть больше 10 и меньше 100 символов")
        ));
        List<Violation> actualViolations = objectMapper.readValue(response, new TypeReference<>() {
        });

        Assertions.assertEquals(expectedViolations, actualViolations);
    }

    @Test
    @DisplayName("Обновить чужой пост")
    @WithUserDetails(value = "test2@test.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void updatePost_If_Someone_Else() throws Exception {
        PostEntity savePost = getPost1();
        savePost.setUser(userRepository.getByEmail("test@test.com"));
        PostEntity post = postRepository.save(savePost);

        String content = objectMapper.writeValueAsString(getPostUpdateDto());

        String endpoint = this.endpoint + "/" + post.getId();

        MockHttpServletRequestBuilder request = patch(endpoint)
                .content(content)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8);

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    private PostUpdateDto getPostUpdateDto() {
        return PostUpdateDto.builder()
                .text("updated text")
                .build();
    }

    private PostUpdateDto getInvalidPostUpdateDto() {
        return PostUpdateDto.builder()
                .title("test")
                .build();
    }

    private PostCreationDto getInvalidPostCreationDto() {
        return PostCreationDto.builder()
                .title("test")
                .text("test post creation  text")
                .build();
    }

    private PostCreationDto getPostCreationDto() {
        return PostCreationDto.builder()
                .title("test post creation title")
                .text("test post creation text")
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

    private PostEntity getPost1() {
        return PostEntity.builder()
                .title("test title")
                .text("test text")
                .images(new HashSet<>())
                .build();
    }

    private PostEntity getPost2() {
        return PostEntity.builder()
                .title("test title 2")
                .text("test text 2")
                .user(getUser1())
                .images(new HashSet<>())
                .build();
    }
}
