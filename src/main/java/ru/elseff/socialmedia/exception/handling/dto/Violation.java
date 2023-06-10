package ru.elseff.socialmedia.exception.handling.dto;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@RequiredArgsConstructor
@EqualsAndHashCode(of = {"fieldName", "message"})
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class Violation {

    String fieldName;

    String message;
}
