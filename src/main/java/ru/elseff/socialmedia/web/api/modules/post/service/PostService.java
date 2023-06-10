package ru.elseff.socialmedia.web.api.modules.post.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.elseff.socialmedia.persistense.PostEntity;
import ru.elseff.socialmedia.persistense.UserEntity;
import ru.elseff.socialmedia.persistense.dao.PostRepository;
import ru.elseff.socialmedia.web.api.modules.user.service.UserService;

import javax.transaction.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PostService {

    PostRepository postRepository;

    UserService userService;

    @Transactional
    public Page<PostEntity> findAll(int size,
                                    int page,
                                    String sortField,
                                    String sortOrder) {
        return postRepository
                .findAll(PageRequest.of(
                        page,
                        size,
                        Sort.by(Sort.Direction.fromString(sortOrder),
                                sortField)));
    }

    @Transactional
    public PostEntity findById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("post not found"));
    }

    @Transactional
    public PostEntity addPost(PostEntity postEntity) {
        UserEntity user = userService.getCurrentAuthUser();
        postEntity.setUser(user);

        return postRepository.save(postEntity);
    }

    @Transactional
    public void deletePost(Long id) {
        PostEntity post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("post not found"));

        if (isCurrentUserOwnPost(post))
            postRepository.deleteById(id);
        else
            throw new IllegalArgumentException("someone else's post");
    }

    @Transactional
    public PostEntity updatePost(Long id, PostEntity post) {
        PostEntity postFromDb = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("post not found"));

        if (isCurrentUserOwnPost(postFromDb)) {
            if (post.getTitle() != null)
                postFromDb.setTitle(post.getTitle());
            if (post.getText() != null)
                postFromDb.setText(post.getText());

            return postRepository.save(postFromDb);
        } else
            throw new IllegalArgumentException("someone else's post");
    }

    @Transactional
    public boolean isCurrentUserOwnPost(PostEntity post) {
        UserEntity user = userService.getCurrentAuthUser();

        return post.getUser().equals(user);
    }

}
