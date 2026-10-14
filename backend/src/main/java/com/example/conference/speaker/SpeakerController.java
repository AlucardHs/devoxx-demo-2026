package com.example.conference.speaker;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;

import java.util.List;
import java.util.Optional;

@Controller("/api/speakers")
public class SpeakerController {

    private final SpeakerRepository speakers;

    public SpeakerController(SpeakerRepository speakers) {
        this.speakers = speakers;
    }

    @Get
    public List<Speaker> list() {
        return speakers.findAllOrderByName();
    }

    @Get("/{id}")
    public Optional<Speaker> get(Long id) {
        return speakers.findById(id);
    }
}
