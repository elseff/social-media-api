package ru.elseff.socialmedia.web.api.modules.post.service;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import ru.elseff.socialmedia.persistense.PostEntity;
import ru.elseff.socialmedia.persistense.UserEntity;
import ru.elseff.socialmedia.persistense.dao.PostRepository;
import ru.elseff.socialmedia.web.api.modules.user.service.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.*;

@FieldDefaults(level = AccessLevel.PRIVATE)
class PostServiceTest {

    @InjectMocks
    PostService postService;

    @Mock
    UserService userService;

    @Mock
    PostRepository postRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Найти все посты")
    void findAll() {
        when(postRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(getPost1(), getPost2())));

        Page<PostEntity> posts = postService.findAll(20, 0, "id", "ASC");

        List<PostEntity> expectedPosts = new ArrayList<>(List.of(getPost1(), getPost2()));
        List<PostEntity> actualPosts = posts.getContent();

        Assertions.assertEquals(expectedPosts, actualPosts);
        verify(postRepository, times(1)).findAll(any(Pageable.class));
        verifyNoMoreInteractions(postRepository);
        verifyNoInteractions(userService);
    }

    @Test
    @DisplayName("Найти пост по id")
    void findById() {
        when(postRepository.findById(anyLong())).thenReturn(java.util.Optional.ofNullable(getPost1()));

        PostEntity expectedPost = getPost1();
        PostEntity actualPost = postService.findById(1L);

        Assertions.assertEquals(expectedPost, actualPost);
        verify(postRepository, times(1)).findById(anyLong());
        verifyNoMoreInteractions(postRepository);
        verifyNoInteractions(userService);
    }

    @Test
    @DisplayName("Найти пост по id, если он не найден")
    void findById_If_Post_Is_Not_Found() {
        when(postRepository.findById(anyLong())).thenReturn(Optional.empty());

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
                () -> postService.findById(0L));

        String exceptedExceptionMessage = "post not found";
        String actualExceptionMessage = exception.getMessage();

        Assertions.assertEquals(exceptedExceptionMessage, actualExceptionMessage);
        verify(postRepository, times(1)).findById(anyLong());
        verifyNoMoreInteractions(postRepository);
        verifyNoInteractions(userService);
    }

    @Test
    @DisplayName("Добавить пост")
    void addPost() {
        when(userService.getCurrentAuthUser()).thenReturn(getUser());
        when(postRepository.save(any(PostEntity.class))).thenReturn(getPostFromDb());

        PostEntity expectedPost = getPostFromDb();
        PostEntity actualPost = postService.addPost(getPost1());

        Assertions.assertEquals(expectedPost, actualPost);
        verify(userService, times(1)).getCurrentAuthUser();
        verify(postRepository, times(1)).save(any(PostEntity.class));
        verifyNoMoreInteractions(userService);
        verifyNoMoreInteractions(postRepository);
    }

    @Test
    @DisplayName("Удалить пост")
    void deletePost() {
        PostEntity post = getPost1();
        post.setUser(getUser());
        given(postRepository.findById(anyLong())).willReturn(Optional.of(post));
        willDoNothing().given(postRepository).deleteById(anyLong());
        given(userService.getCurrentAuthUser()).willReturn(getUser());

        postService.deletePost(1L);

        verify(postRepository, times(1)).deleteById(anyLong());
        verify(postRepository, times(1)).findById(anyLong());
        verify(userService, times(1)).getCurrentAuthUser();
        verifyNoMoreInteractions(postRepository);
        verifyNoMoreInteractions(userService);

    }

    @Test
    @DisplayName("Удалить пост, если он не найден")
    void deletePost_If_Post_Is_Not_Found() {
        given(postRepository.findById(anyLong())).willReturn(Optional.empty());

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
                () -> postService.deletePost(0L));

        String expectedExceptionMessage = "post not found";
        String actualExceptionMessage = exception.getMessage();

        Assertions.assertEquals(expectedExceptionMessage, actualExceptionMessage);
        verify(postRepository, times(1)).findById(anyLong());
        verifyNoMoreInteractions(postRepository);
        verifyNoInteractions(userService);
    }

    @Test
    @DisplayName("Удалить пост, если он чужой")
    void deletePost_If_Someone_Else_Post() {
        PostEntity post = getPost1();
        post.setUser(getUser());
        given(postRepository.findById(anyLong())).willReturn(Optional.of(post));
        given(userService.getCurrentAuthUser()).willReturn(getUser2());

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
                () -> postService.deletePost(0L));

        String expectedExceptionMessage = "someone else's post";
        String actualExceptionMessage = exception.getMessage();

        Assertions.assertEquals(expectedExceptionMessage, actualExceptionMessage);
        verify(postRepository, times(1)).findById(anyLong());
        verify(userService, times(1)).getCurrentAuthUser();
        verifyNoMoreInteractions(postRepository);
        verifyNoMoreInteractions(userService);
    }

    @Test
    @DisplayName("Обновить пост")
    void updatePost() {
        given(userService.getCurrentAuthUser()).willReturn(getUser());
        given(postRepository.findById(anyLong())).willReturn(Optional.ofNullable(getPost1()));
        given(postRepository.save(any(PostEntity.class))).willReturn(getUpdatedPostFromDb());

        PostEntity expectedUpdatedPost = getUpdatedPostFromDb();
        PostEntity actualUpdatedPost = postService.updatePost(1L, getUpdatedPostFromDb());

        Assertions.assertEquals(expectedUpdatedPost, actualUpdatedPost);
        verify(userService, times(1)).getCurrentAuthUser();
        verify(postRepository, times(1)).findById(anyLong());
        verify(postRepository, times(1)).save(any(PostEntity.class));
        verifyNoMoreInteractions(userService);
        verifyNoMoreInteractions(postRepository);
    }

    @Test
    @DisplayName("Обновить пост, если он не найден")
    void updatePost_If_Post_Is_Not_Found() {
        given(postRepository.findById(anyLong())).willReturn(Optional.empty());

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
                () -> postService.updatePost(0L, getUpdatedPostFromDb()));

        String expectedExceptionMessage = "post not found";
        String actualExceptionMessage = exception.getMessage();

        Assertions.assertEquals(expectedExceptionMessage, actualExceptionMessage);
        verify(postRepository, times(1)).findById(anyLong());
        verifyNoMoreInteractions(postRepository);
        verifyNoInteractions(userService);
    }

    @Test
    @DisplayName("Обновить пост, если он чужой")
    void updatePost_If_Post_If_Someone_Else() {
        given(postRepository.findById(anyLong())).willReturn(Optional.ofNullable(getPostFromDb()));
        given(userService.getCurrentAuthUser()).willReturn(getUser2());

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
                () -> postService.updatePost(1L, getUpdatedPostFromDb()));

        String expectedExceptionMessage = "someone else's post";
        String actualExceptionMessage = exception.getMessage();

        Assertions.assertEquals(expectedExceptionMessage, actualExceptionMessage);
        verify(postRepository, times(1)).findById(anyLong());
        verify(userService, times(1)).getCurrentAuthUser();
        verifyNoMoreInteractions(postRepository);
        verifyNoMoreInteractions(userService);
    }

    private PostEntity getPostFromDb() {
        return PostEntity.builder()
                .id(getPost1().getId())
                .title(getPost1().getTitle())
                .text(getPost1().getText())
                .user(getUser())
                .build();
    }

    private PostEntity getUpdatedPostFromDb() {
        PostEntity post = getPostFromDb();
        post.setText("updated post");
        return post;
    }

    private UserEntity getUser2() {
        return UserEntity.builder()
                .id(2L)
                .email("test2@test.com")
                .username("test username 2")
                .password("test password 2")
                .build();
    }


    private UserEntity getUser() {
        return UserEntity.builder()
                .id(1L)
                .email("test@test.com")
                .username("test username")
                .password("test password")
                .build();
    }

    private PostEntity getPost1() {
        return PostEntity.builder()
                .id(1L)
                .title("test post title")
                .text("test post text")
                .build();
    }

    private PostEntity getPost2() {
        return PostEntity.builder()
                .id(2L)
                .title("test post title 2")
                .text("test post text 2")
                .build();
    }
}