package ru.elseff.socialmedia.web.api.modules.post.dto.mapper;

import lombok.Getter;
import lombok.Setter;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;
import ru.elseff.socialmedia.persistense.PostEntity;
import ru.elseff.socialmedia.web.api.modules.post.controller.PostController;
import ru.elseff.socialmedia.web.api.modules.post.dto.PostCreationDto;
import ru.elseff.socialmedia.web.api.modules.post.dto.PostDto;
import ru.elseff.socialmedia.web.api.modules.post.dto.PostUpdateDto;
import ru.elseff.socialmedia.web.api.modules.postimage.dto.PostImageDto;
import ru.elseff.socialmedia.web.api.modules.user.dto.UserDto;

import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.stream.Collectors;

@Getter
@Setter
@Component
public class PostDtoAssembler extends RepresentationModelAssemblerSupport<PostEntity, PostDto> {
    public PostDtoAssembler() {
        super(PostController.class, PostDto.class);
    }

    public PostDto mapPostEntityToDto(PostEntity postEntity) {
        return PostDto.builder()
                .id(postEntity.getId())
                .title(postEntity.getTitle())
                .text(postEntity.getText())
                .createdAt(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(postEntity.getCreatedAt()))
                .updatedAt(postEntity.getUpdatedAt() != null
                        ? new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(postEntity.getUpdatedAt())
                        : null)
                .author(UserDto.builder()
                        .id(postEntity.getUser().getId())
                        .username(postEntity.getUser().getUsername())
                        .build())
                .images(postEntity.getImages().stream().map(image ->
                        PostImageDto.builder()
                                .id(image.getId())
                                .filename(image.getFilename())
                                .build()
                ).collect(Collectors.toList()))
                .build();
    }

    public PostEntity mapCreationDtoToPostEntity(PostCreationDto creationDto) {
        return PostEntity.builder()
                .title(creationDto.getTitle())
                .text(creationDto.getText())
                .images(new HashSet<>())
                .build();
    }

    public PostEntity mapUpdateDtoToPostEntity(PostUpdateDto updateDto) {
        return PostEntity.builder()
                .title(updateDto.getTitle())
                .text(updateDto.getText())
                .images(new HashSet<>())
                .build();
    }

    @Override
    public PostDto toModel(PostEntity entity) {
        return mapPostEntityToDto(entity);
    }
}
