package com.example.conference.chat;

import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@Controller("/api/chat")
public class ChatController {

    @Serdeable
    public record ChatRequest(@NotBlank String conversationId, @NotBlank String message) {
    }

    @Serdeable
    public record ChatResponse(String reply) {
    }

    private final ConferenceAssistant assistant;

    public ChatController(ConferenceAssistant assistant) {
        this.assistant = assistant;
    }

    @Post
    @ExecuteOn(TaskExecutors.BLOCKING)
    public ChatResponse chat(@Body @Valid ChatRequest request) {
        return new ChatResponse(assistant.chat(request.conversationId(), request.message()));
    }
}
