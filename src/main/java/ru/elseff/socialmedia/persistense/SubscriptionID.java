package ru.elseff.socialmedia.persistense;

import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@Builder
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SubscriptionID implements Serializable {

    Long userId;

    Long subscriberId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SubscriptionID that)) return false;
        return getUserId().equals(that.getUserId()) && getSubscriberId().equals(that.getSubscriberId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUserId(), getSubscriberId());
    }
}
