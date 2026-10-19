package com.example.conference.chat;

import dev.langchain4j.service.Result;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

@Controller("/api/chat")
public class ChatController {

    @Serdeable
    public record ChatRequest(@NotBlank String conversationId, @NotBlank String message) {
    }

    @Serdeable
    public record ToolCall(String name, String arguments) {
    }

    @Serdeable
    public record ChatResponse(String reply, List<ToolCall> toolCalls) {
    }

    private final ConferenceAssistant assistant;

    public ChatController(ConferenceAssistant assistant) {
        this.assistant = assistant;
    }

    @Post
    @ExecuteOn(TaskExecutors.BLOCKING)
    public ChatResponse chat(@Body @Valid ChatRequest request) {
        Result<String> result = assistant.chat(request.conversationId(), request.message());
        List<ToolCall> toolCalls = result.toolExecutions().stream()
                .map(execution -> new ToolCall(execution.request().name(), execution.request().arguments()))
                .toList();
        return new ChatResponse(result.content(), toolCalls);
    }
}
