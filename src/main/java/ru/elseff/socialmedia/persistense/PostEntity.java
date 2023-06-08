package ru.elseff.socialmedia.persistense;

import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Objects;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "post_entity", schema = "public")
public class PostEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    Long id;

    @Column(name = "title", nullable = false)
    String title;

    @Column(name = "text", nullable = false)
    String text;

    @Column(name = "created_at", nullable = false, updatable = false)
    Timestamp createdAt;

    @Column(name = "updated_at")
    Timestamp updatedAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    UserEntity user;

    @OneToMany(mappedBy = "post",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    Set<PostImageEntity> images;

    @PrePersist
    public void prePersist() {
        createdAt = Timestamp.from(Instant.now());
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Timestamp.from(Instant.now());
    }

    @Override
    public String toString() {
        return "PostEntity{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", text='" + text + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PostEntity that)) return false;
        return getId().equals(that.getId())
                && getTitle().equals(that.getTitle())
                && getText().equals(that.getText());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getTitle(), getText());
    }
}
