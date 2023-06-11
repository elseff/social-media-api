package ru.elseff.socialmedia.web.api.modules.postimage.service;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import ru.elseff.socialmedia.persistense.PostEntity;
import ru.elseff.socialmedia.persistense.PostImageEntity;
import ru.elseff.socialmedia.persistense.dao.PostImageRepository;
import ru.elseff.socialmedia.web.api.modules.post.service.PostService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@FieldDefaults(level = AccessLevel.PRIVATE)
class PostImageServiceTest {
    @InjectMocks
    PostImageService postImageService;

    @Mock
    PostService postService;

    @Mock
    PostImageRepository postImageRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    @DisplayName("Очистка тестовых изображений")
    void tearDown() {
        Path path = postImageService.getRoot();
        try {
            String encodedFilename = Base64.getEncoder().encodeToString("test".getBytes()) + ".test";
            if (Files.exists(path.resolve(encodedFilename)))
                Files.delete(postImageService.getRoot().resolve(encodedFilename));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("Найти все изображения поста")
    void findAllByPostId() {
        given(postService.findById(anyLong())).willReturn(getPostEntity1());
        given(postImageRepository.findAllByPost(any(PostEntity.class))).willReturn(List.of(getPostImageEntity1(), getPostImageEntity2()));

        List<PostImageEntity> expectedImagesList = new ArrayList<>(List.of(getPostImageEntity1(), getPostImageEntity2()));
        List<PostImageEntity> actualImagesList = postImageService.findAllByPostId(1L);

        Assertions.assertEquals(expectedImagesList, actualImagesList);
        verify(postService, times(1)).findById(anyLong());
        verify(postImageRepository, times(1)).findAllByPost(any(PostEntity.class));
        verifyNoMoreInteractions(postService);
        verifyNoMoreInteractions(postImageRepository);
    }

    @Test
    @DisplayName("Найти все изображения, если пост не найден")
    void findAllByPostId_If_Post_Is_Not_Found() {
        given(postService.findById(anyLong())).willThrow(new IllegalArgumentException("post not found"));

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
                () -> postImageService.findAllByPostId(1L));

        String expectedExceptionMessage = "post not found";
        String actualExceptionMessage = exception.getMessage();

        Assertions.assertEquals(expectedExceptionMessage, actualExceptionMessage);
        verify(postService, times(1)).findById(anyLong());
        verifyNoMoreInteractions(postService);
        verifyNoInteractions(postImageRepository);
    }

    @Test
    @DisplayName("Загрузить изображение")
    void uploadPostImage() {
        MultipartFile file = getMultipartFile();
        given(postService.findById(anyLong())).willReturn(getPostEntity1());
        given(postImageRepository.save(any(PostImageEntity.class))).willReturn(getPostImageEntity1());

        postImageService.uploadPostImage(file, 1L);

    }

    @Test
    @DisplayName("Загрузить изображение, если пост не найден")
    void uploadPostImage_If_Post_Is_Not_Found() {
        MultipartFile file = getMultipartFile();
        given(postService.findById(anyLong())).willThrow(new IllegalArgumentException("post not found"));

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
                () -> postImageService.uploadPostImage(file, 1L));

        String expectedExceptionMessage = "post not found";
        String actualExceptionMessage = exception.getMessage();

        Assertions.assertEquals(expectedExceptionMessage, actualExceptionMessage);
        verify(postService, times(1)).findById(anyLong());
        verifyNoMoreInteractions(postService);
        verifyNoInteractions(postImageRepository);
    }

    private MultipartFile getMultipartFile() {
        return new MockMultipartFile("test",
                "test.test",
                MediaType.IMAGE_PNG_VALUE,
                "test".getBytes(StandardCharsets.UTF_8));
    }

    private PostImageEntity getPostImageEntity1() {
        return PostImageEntity.builder()
                .post(getPostEntity1())
                .filename(getMultipartFile().getOriginalFilename())
                .build();
    }

    private PostImageEntity getPostImageEntity2() {
        return PostImageEntity.builder()
                .post(getPostEntity2())
                .filename("testfilename2.png")
                .build();
    }

    private PostEntity getPostEntity1() {
        return PostEntity.builder()
                .id(1L)
                .title("test title 1")
                .text("test text 1")
                .build();
    }

    private PostEntity getPostEntity2() {
        return PostEntity.builder()
                .id(2L)
                .title("test title 2")
                .text("test text 2")
                .build();
    }
}