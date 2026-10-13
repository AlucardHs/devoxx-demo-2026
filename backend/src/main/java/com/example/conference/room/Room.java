package com.example.conference.room;

import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.serde.annotation.Serdeable;

@Serdeable
@MappedEntity("room")
public record Room(@Id Long id, String name, int floor, int capacity) {
}
