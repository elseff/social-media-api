package ru.elseff.socialmedia.web.api.modules.postimage.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.elseff.socialmedia.persistense.PostEntity;
import ru.elseff.socialmedia.persistense.UserEntity;
import ru.elseff.socialmedia.persistense.dao.UserRepository;
import ru.elseff.socialmedia.web.api.modules.post.service.PostService;
import ru.elseff.socialmedia.web.api.modules.postimage.dto.PostImageDto;
import ru.elseff.socialmedia.web.api.modules.postimage.dto.mapper.PostImageDtoMapper;
import ru.elseff.socialmedia.web.api.modules.postimage.service.PostImageService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Comparator;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@FieldDefaults(level = AccessLevel.PRIVATE)
class PostImageControllerTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest");

    @Autowired
    PostImageDtoMapper postImageDtoMapper;

    @Autowired
    PostImageService postImageService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    PostService postService;

    @Autowired
    MockMvc mockMvc;

    final String endpoint = "/api/v1/posts/%s/images/";

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
    }

    @Test
    @DisplayName("Context loads")
    void contextLoads() {
        Assertions.assertNotNull(postImageService);
        Assertions.assertNotNull(userRepository);
        Assertions.assertNotNull(postService);
        Assertions.assertNotNull(mockMvc);
    }

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        userRepository.save(getUser1());
    }

    @AfterEach
    @DisplayName("Очистка тестовых изображений")
    void tearDown() {
        Path path = postImageService.getRoot();
        try {
            String encodedFilename = Base64.getEncoder().encodeToString("test".getBytes()) + ".test";
            if (Files.exists(path.resolve(encodedFilename)))
                Files.delete(postImageService.getRoot().resolve(encodedFilename));
            encodedFilename = Base64.getEncoder().encodeToString("test2".getBytes()) + ".test";
            if (Files.exists(path.resolve(encodedFilename)))
                Files.delete(postImageService.getRoot().resolve(encodedFilename));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("Найти все изображения поста")
    @WithUserDetails(value = "test@test.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void findAllByPostId() throws Exception {
        PostEntity post = postService.addPost(getPost1());
        postImageService.uploadPostImage(getMultipartFile1(), post.getId());
        postImageService.uploadPostImage(getMultipartFile2(), post.getId());

        String endpoint = String.format(this.endpoint, post.getId());

        MockHttpServletRequestBuilder request = get(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8);

        String response = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        Set<PostImageDto> images = objectMapper.readValue(response, new TypeReference<>() {
        });

        Assertions.assertNotNull(images);
        Assertions.assertEquals(2, images.size());
        Assertions.assertEquals("test.test", images
                .stream()
                .sorted(Comparator.comparing(PostImageDto::getId))
                .iterator()
                .next()
                .getFilename());
    }

    @Test
    @DisplayName("Найти все изображения поста, если он не найден")
    @WithUserDetails(value = "test@test.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void findAllByPostId_If_Post_Is_Not_Found() throws Exception {
        String endpoint = String.format(this.endpoint, 0);

        MockHttpServletRequestBuilder request = get(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8);

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Загрузить изображение")
    @WithUserDetails(value = "test@test.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void uploadImage() throws Exception {
        PostEntity post = postService.addPost(getPost1());
        String endpoint = String.format(this.endpoint + "/upload", post.getId());

        MockHttpServletRequestBuilder request = multipart(endpoint)
                .file(getMultipartFile1())
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .characterEncoding(StandardCharsets.UTF_8);

        String response = mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        PostImageDto imageDto = objectMapper.readValue(response, PostImageDto.class);

        String expectedFilename = getMultipartFile1().getOriginalFilename();
        String actualFilename = imageDto.getFilename();

        Assertions.assertEquals(expectedFilename,actualFilename);
    }

    @Test
    @DisplayName("Загрузить изображение, если пост не найден")
    @WithUserDetails(value = "test@test.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void uploadFile_If_Post_If_Not_Found() throws Exception {
        String endpoint = String.format(this.endpoint + "/upload", 0);

        MockHttpServletRequestBuilder request = multipart(endpoint)
                .file(getMultipartFile1())
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .characterEncoding(StandardCharsets.UTF_8);

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    private UserEntity getUser1() {
        return UserEntity.builder()
                .email("test@test.com")
                .username("test_username")
                .password("test_password")
                .build();
    }

    private MockMultipartFile getMultipartFile1() {
        return new MockMultipartFile("image",
                "test.test",
                MediaType.IMAGE_PNG_VALUE,
                "test".getBytes(StandardCharsets.UTF_8));
    }

    private MockMultipartFile getMultipartFile2() {
        return new MockMultipartFile("image",
                "test2.test",
                MediaType.IMAGE_PNG_VALUE,
                "test2".getBytes(StandardCharsets.UTF_8));
    }

    private PostEntity getPost2() {
        return PostEntity.builder()
                .title("test title 2")
                .text("test text 2")
                .build();
    }

    private PostEntity getPost1() {
        return PostEntity.builder()
                .title("test title 1")
                .text("test text 1")
                .build();
    }
}