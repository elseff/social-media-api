package ru.elseff.socialmedia.web.api.modules.postimage.dto.mapper;

import org.springframework.stereotype.Component;
import ru.elseff.socialmedia.persistense.PostImageEntity;
import ru.elseff.socialmedia.web.api.modules.postimage.dto.PostImageDto;

@Component
public class PostImageDtoMapper {

    public PostImageDto mapPostImageEntityToDto(PostImageEntity imageEntity) {
        return PostImageDto.builder()
                .id(imageEntity.getId())
                .filename(imageEntity.getFilename())
                .build();
    }
}
