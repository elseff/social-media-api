package ru.elseff.socialmedia.web.api.modules.postimage.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.elseff.socialmedia.persistense.PostImageEntity;
import ru.elseff.socialmedia.web.api.modules.postimage.dto.PostImageDto;
import ru.elseff.socialmedia.web.api.modules.postimage.dto.mapper.PostImageDtoMapper;
import ru.elseff.socialmedia.web.api.modules.postimage.service.PostImageService;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/posts/{postId}/images")
@SecurityRequirement(name = "Bearer Authentication")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Post Image Controller", description = "Управление изображениями постов")
public class PostImageController {

    PostImageService postImageService;

    PostImageDtoMapper postImageDtoMapper;

    @Operation(
            method = "GET",
            summary = "Найти изображения",
            description = "Найти изображения поста, указав его id",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Изображения поста успешно найдены",
                            content = @Content(
                                    schema = @Schema(implementation = PostImageDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Пост не найден",
                            content = @Content
                    )
            }
    )
    @GetMapping(consumes = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public Set<PostImageDto> findAllByPostId(@Parameter(description = "id поста", required = true)
                                             @PathVariable("postId") Long postId) {
        List<PostImageEntity> images = postImageService.findAllByPostId(postId);

        return images
                .stream()
                .map(postImageDtoMapper::mapPostImageEntityToDto)
                .collect(Collectors.toSet());
    }

    @Operation(
            method = "POST",
            summary = "Загрузить изображение",
            description = "Загрузить изображение, указав  id поста и прикрепив файл с изображением",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Успешно",
                            content = @Content(
                                    schema = @Schema(implementation = PostImageDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Пост не найден",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Не удалось загрузить изображение",
                            content = @Content
                    )
            }
    )
    @PostMapping(value = "/upload", consumes = "multipart/form-data", produces = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    public PostImageDto uploadImage(
            @Parameter(description = "id поста", required = true)
            @PathVariable("postId") Long postId,
            @Parameter(description = "Файл с изображением", required = true,
                    content = @Content(
                            mediaType = "multipart/form-data",
                            schema = @Schema(
                                    implementation = MultipartFile.class
                            )
                    ))
            @RequestParam("image") MultipartFile multipartFile) {
        PostImageEntity image = postImageService.uploadPostImage(multipartFile, postId)
                .orElseThrow(() -> new RuntimeException("something wrong"));

        return postImageDtoMapper.mapPostImageEntityToDto(image);
    }
}
