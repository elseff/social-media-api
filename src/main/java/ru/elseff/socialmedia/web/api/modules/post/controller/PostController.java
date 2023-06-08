package ru.elseff.socialmedia.web.api.modules.post.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.elseff.socialmedia.persistense.PostEntity;
import ru.elseff.socialmedia.web.api.modules.post.dto.PostCreationDto;
import ru.elseff.socialmedia.web.api.modules.post.dto.PostDto;
import ru.elseff.socialmedia.web.api.modules.post.dto.PostUpdateDto;
import ru.elseff.socialmedia.web.api.modules.post.dto.mapper.PostDtoAssembler;
import ru.elseff.socialmedia.web.api.modules.post.service.PostService;

import javax.validation.Valid;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/posts")
@SecurityRequirement(name = "Bearer Authentication")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Post Controller", description = "Управление постами")
public class PostController {

    PostService postService;

    PagedResourcesAssembler<PostEntity> pagedResourcesAssembler;

    PostDtoAssembler postDtoAssembler;

    @Operation(
            method = "GET",
            summary = "Лента активности",
            description = "Посты пользователей",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Посты успешно найдены",
                            content = @Content(
                                    schema = @Schema(implementation = PostDto.class)
                            )
                    )
            }
    )
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public PagedModel<PostDto> findAll(@Parameter(description = "Номер страницы")
                                       @RequestParam(defaultValue = "0", required = false) int page,
                                       @Parameter(description = "Размер страницы. Количество элементов в ней")
                                       @RequestParam(defaultValue = "20", required = false) int size,
                                       @Parameter(description = "Поле, по которому будет идти сортировка")
                                       @RequestParam(defaultValue = "id", required = false) String sortField,
                                       @Parameter(description = "Сортировка (ASC - по возрастанию, DESC - по убыванию)")
                                       @RequestParam(defaultValue = "ASC", required = false) Sort.Direction sortOrder) {
        Page<PostEntity> posts = postService.findAll(size, page, sortField, sortOrder.toString());

        return pagedResourcesAssembler.toModel(posts, postDtoAssembler);
    }

    @Operation(
            method = "GET",
            summary = "Пост по id",
            description = "Получить пост, указав его id",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Пост успешно найден",
                            content = @Content(
                                    schema = @Schema(implementation = PostDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Пост не найден",
                            content = @Content
                    )
            }

    )
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public PostDto findById(@Parameter(description = "id поста", required = true)
                            @PathVariable Long id) {
        PostEntity post = postService.findById(id);

        return postDtoAssembler.mapPostEntityToDto(post);
    }

    @Operation(
            method = "POST",
            summary = "Добавить пост",
            description = "Добавить пост указав название и текст",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Пост успешно создан",
                            content = @Content(
                                    schema = @Schema(implementation = PostDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Неверные данные для создания поста",
                            content = @Content
                    )
            }

    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PostDto addPost(@RequestBody(description = "Данные для создания поста",
            content = @Content(schema = @Schema(implementation = PostCreationDto.class)))
                           @org.springframework.web.bind.annotation.RequestBody @Valid PostCreationDto postDto) {
        PostEntity post = postDtoAssembler.mapCreationDtoToPostEntity(postDto);
        PostEntity result = postService.addPost(post);

        return postDtoAssembler.mapPostEntityToDto(result);
    }

    @Operation(
            method = "DELETE",
            summary = "Удалить пост",
            description = "Удалить пост указав его id",
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "Пост успешно удалён",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Пост не найден",
                            content = @Content
                    )
            }

    )
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePost(@Parameter(description = "id поста", required = true)
                           @PathVariable Long id) {
        postService.deletePost(id);
    }

    @Operation(
            method = "PATCH",
            summary = "Обновить пост",
            description = "Обновить название или текст поста, указав его id",
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "Пост успешно обновлён",
                            content = @Content(
                                    schema = @Schema(implementation = PostDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Пост не найден",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Неверные данные для обновления поста",
                            content = @Content
                    )
            }

    )
    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public PostDto updatePost(@Parameter(description = "id поста", required = true)
                              @PathVariable Long id,
                              @RequestBody(description = "Данные для обновления поста",
                                      content = @Content(
                                              schema = @Schema(implementation = PostUpdateDto.class)))
                              @org.springframework.web.bind.annotation.RequestBody @Valid PostUpdateDto post) {
        PostEntity postEntity = postDtoAssembler.mapUpdateDtoToPostEntity(post);
        PostEntity result = postService.updatePost(id, postEntity);

        return postDtoAssembler.mapPostEntityToDto(result);
    }
}
