package ru.elseff.socialmedia.persistense;

import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;
import java.sql.Timestamp;

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
}
