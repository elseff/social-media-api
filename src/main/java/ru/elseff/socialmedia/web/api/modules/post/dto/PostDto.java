package ru.elseff.socialmedia.web.api.modules.post.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;
import ru.elseff.socialmedia.web.api.modules.postimage.dto.PostImageDto;
import ru.elseff.socialmedia.web.api.modules.user.dto.UserDto;

import java.util.List;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "Post entity dto")
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Relation(collectionRelation = "posts", itemRelation = "post")
public class PostDto extends RepresentationModel<PostDto> {

    Long id;

    String title;

    String text;

    UserDto author;

    String createdAt;

    String updatedAt;

    List<PostImageDto> images;
}
