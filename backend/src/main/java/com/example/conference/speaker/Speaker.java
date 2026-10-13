package com.example.conference.speaker;

import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.serde.annotation.Serdeable;

@Serdeable
@MappedEntity("speaker")
public record Speaker(@Id Long id, String name, String jobTitle, String company, String country, String bio) {
}
