package com.example.conference.chat;

import io.micronaut.serde.annotation.Serdeable;

import java.util.List;

@Serdeable
public record ChatResponse(String reply, List<ToolCall> toolCalls) {

    @Serdeable
    public record ToolCall(String name, String arguments) {
    }
}
