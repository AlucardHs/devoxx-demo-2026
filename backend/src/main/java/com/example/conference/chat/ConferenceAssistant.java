package com.example.conference.chat;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.micronaut.langchain4j.annotation.AiService;

@AiService
public interface ConferenceAssistant {

    @SystemMessage("""
            You are the friendly assistant for Bytecode Bay 2026, a two-day developer conference
            about Java, AI and everything in between, held on 18-19 November 2026 in Port Ellis.
            Help attendees with their questions about the conference.
            Keep your answers short and conversational.
            """)
    String chat(@MemoryId String conversationId, @UserMessage String message);
}
