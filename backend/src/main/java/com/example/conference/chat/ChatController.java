package com.example.conference.chat;

import dev.langchain4j.guardrail.GuardrailException;
import dev.langchain4j.service.Result;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Controller("/api/chat")
public class ChatController {

    private static final Logger LOG = LoggerFactory.getLogger(ChatController.class);
    private static final String REFUSAL =
            "Sorry, I can only help with questions about the Bytecode Bay 2026 programme: talks, speakers, rooms and times.";

    private final ConferenceAssistant assistant;

    public ChatController(ConferenceAssistant assistant) {
        this.assistant = assistant;
    }

    @Post
    @ExecuteOn(TaskExecutors.BLOCKING)
    public ChatResponse chat(@Body @Valid ChatRequest request) {
        Result<String> result;
        try {
            result = assistant.chat(request.conversationId(), request.message());
        } catch (GuardrailException e) {
            LOG.info("Guardrail blocked conversation {}: {}", request.conversationId(), e.getMessage());
            return new ChatResponse(REFUSAL, List.of());
        }
        List<ChatResponse.ToolCall> toolCalls = result.toolExecutions().stream()
                .map(execution -> new ChatResponse.ToolCall(execution.request().name(), execution.request().arguments()))
                .toList();
        return new ChatResponse(result.content(), toolCalls);
    }
}
