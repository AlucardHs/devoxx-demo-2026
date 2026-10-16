package com.example.conference.chat;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;

@Serdeable
public record ChatRequest(@NotBlank String conversationId, @NotBlank String message) {
}
