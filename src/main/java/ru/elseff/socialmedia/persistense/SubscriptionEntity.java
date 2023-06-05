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
@Table(name = "subscription_entity", schema = "public")
public class SubscriptionEntity {

    @EmbeddedId
    SubscriptionID id;

    @Column(name = "accepted", nullable = false)
    Boolean accepted;

    @ManyToOne
    @MapsId("userId")
    UserEntity user;

    @ManyToOne
    @MapsId("subscriberId")
    UserEntity subscriber;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SubscriptionEntity that)) return false;
        return getId().equals(that.getId()) && getAccepted().equals(that.getAccepted());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getAccepted());
    }
}
