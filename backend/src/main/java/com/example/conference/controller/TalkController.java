package com.example.conference.controller;

import com.example.conference.model.Talk;
import com.example.conference.repository.TalkRepository;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;

import java.util.List;
import java.util.Optional;

@Controller("/api/talks")
public class TalkController {

    private final TalkRepository talks;

    public TalkController(TalkRepository talks) {
        this.talks = talks;
    }

    @Get
    public List<Talk> list() {
        return talks.findAllOrderByStartsAt();
    }

    @Get("/{id}")
    public Optional<Talk> get(Long id) {
        return talks.findById(id);
    }
}
