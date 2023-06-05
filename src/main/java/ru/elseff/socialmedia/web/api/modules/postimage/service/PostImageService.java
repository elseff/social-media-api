package ru.elseff.socialmedia.web.api.modules.postimage.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.elseff.socialmedia.persistense.PostEntity;
import ru.elseff.socialmedia.persistense.PostImageEntity;
import ru.elseff.socialmedia.persistense.dao.PostImageRepository;
import ru.elseff.socialmedia.web.api.modules.post.service.PostService;

import javax.transaction.Transactional;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;

import static java.util.Objects.requireNonNull;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PostImageService {

    PostService postService;

    PostImageRepository postImageRepository;

    Path root = Paths.get("./uploads/postimages");

    @Transactional
    public List<PostImageEntity> findAllByPostId(Long postId) {
        PostEntity post = postService.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("post not found"));

        return postImageRepository.findAllByPost(post);
    }

    @Transactional
    public PostImageEntity uploadPostImage(MultipartFile file, Long postId) {
        PostEntity post = postService.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("post not found"));

        PostImageEntity image = PostImageEntity.builder()
                .post(post)
                .filename(file.getOriginalFilename())
                .build();

        try {
            String filename = requireNonNull(file.getOriginalFilename());
            String[] partsFileName = filename.split("\\.");
            String name = partsFileName[0];
            String extension = partsFileName[1];
            String encodeFilename = Base64.getEncoder().encodeToString(name.getBytes(StandardCharsets.UTF_8)) + "." + extension;
            Path path = root.resolve(encodeFilename);
            Files.copy(file.getInputStream(), path);
            log.info("file saved successfully");
        } catch (IOException e) {
            e.printStackTrace();
            log.error("failed to save file {}", file.getOriginalFilename());
        }
        return postImageRepository.save(image);
    }

    @Transactional
    public String deleteImage(Long postId, Long imageId) {
        PostEntity post = postService.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("post not found"));

        PostImageEntity image = postImageRepository.findById(imageId)
                .orElseThrow(() -> new IllegalArgumentException("image " + imageId + " not found"));

        String[] partsFileName = image.getFilename().split("\\.");
        String name = partsFileName[0];
        String extension = partsFileName[1];

        String encodedFilename = Base64.getEncoder().encodeToString(name.getBytes(StandardCharsets.UTF_8));

        Path path = root.resolve(encodedFilename + "." + extension);

        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            e.printStackTrace();
        }

        postImageRepository.delete(image);
        return "deletion successful";
    }
}
