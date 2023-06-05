package ru.elseff.socialmedia.web.api.modules.post.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Size;

@Getter
@Setter
@Builder
@ToString
@Validated
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PostUpdateDto {

    @Size(min = 10, max = 100, message = "Размер названия поста должен быть больше 10 и меньше 100 символов")
    String title;

    @Size(min = 10, max = 1000, message = "Размер описания поста должен быть больше 10 и меньше 1000 символов")
    String text;
}
