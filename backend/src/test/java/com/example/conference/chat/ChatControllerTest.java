package com.example.conference.chat;

import com.example.conference.chat.ChatController.ChatRequest;
import com.example.conference.chat.ChatController.ChatResponse;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@MicronautTest
class ChatControllerTest {

    @Inject
    @Client("/")
    HttpClient client;

    @Test
    void returnsTheAssistantReply() {
        ChatResponse response = client.toBlocking().retrieve(
                HttpRequest.POST("/api/chat", new ChatRequest("abc-123", "Hello!")), ChatResponse.class);

        assertEquals("[abc-123] You said: Hello!", response.reply());
    }

    @Test
    void rejectsAnEmptyMessage() {
        HttpClientResponseException e = assertThrows(HttpClientResponseException.class, () ->
                client.toBlocking().retrieve(HttpRequest.POST("/api/chat", new ChatRequest("abc-123", " ")), ChatResponse.class));

        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
    }

    // Stands in for the real model, so the tests never call Anthropic.
    @Singleton
    @Replaces(ConferenceAssistant.class)
    static class FakeAssistant implements ConferenceAssistant {
        @Override
        public String chat(String conversationId, String message) {
            return "[" + conversationId + "] You said: " + message;
        }
    }
}
