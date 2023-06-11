package ru.elseff.socialmedia.persistense;

import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "post_image_entity", schema = "public")
public class PostImageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "post_id", nullable = false)
    PostEntity post;

    @Column(name = "filename", nullable = false)
    String filename;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PostImageEntity that)) return false;
        return Objects.equals(getId(), that.getId())
                && Objects.equals(getPost(), that.getPost())
                && Objects.equals(getFilename(), that.getFilename());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getPost(), getFilename());
    }
}
