package com.example.conference.model;

import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.sql.JoinColumn;
import io.micronaut.data.annotation.sql.JoinTable;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.Relation;
import io.micronaut.serde.annotation.Serdeable;

import java.time.LocalDateTime;
import java.util.List;

@Serdeable
@MappedEntity("talk")
public record Talk(
        @Id Long id,
        String title,
        String description,
        String track,
        String level,
        String format,
        LocalDateTime startsAt,
        LocalDateTime endsAt,
        @Relation(Relation.Kind.MANY_TO_ONE) Room room,
        @Relation(Relation.Kind.MANY_TO_MANY)
        @JoinTable(
                name = "talk_speaker",
                joinColumns = @JoinColumn(name = "talk_id"),
                inverseJoinColumns = @JoinColumn(name = "speaker_id"))
        List<Speaker> speakers) {
}
