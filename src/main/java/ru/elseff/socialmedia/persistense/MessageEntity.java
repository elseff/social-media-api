package ru.elseff.socialmedia.persistense;

import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "message_entity", schema = "public")
public class MessageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    Long id;

    @Column(name = "text", nullable = false)
    String text;

    @Column(name = "send_at", nullable = false)
    Timestamp sendAt;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    UserEntity sender;

    @ManyToOne
    @JoinColumn(name = "recipient_id", nullable = false)
    UserEntity recipient;

    @Override
    public String toString() {
        return "MessageEntity{" +
                "id=" + id +
                ", text='" + text + '\'' +
                ", sendAt=" + sendAt +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MessageEntity that)) return false;
        return Objects.equals(getId(), that.getId())
                && Objects.equals(getText(), that.getText())
                && Objects.equals(getSendAt(), that.getSendAt());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getText(), getSendAt());
    }
}
