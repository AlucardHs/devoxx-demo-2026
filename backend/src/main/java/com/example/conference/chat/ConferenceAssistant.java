package com.example.conference.chat;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.Result;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.micronaut.langchain4j.annotation.AiService;

@AiService(tools = ConferenceTools.class)
public interface ConferenceAssistant {

    @SystemMessage("""
            You are the friendly assistant for Bytecode Bay 2026, a developer conference \
            held on 18–19 November 2026 in Port Ellis.
            Answer questions about the talks, speakers, rooms and schedule using the tools; \
            never guess details that you have not looked up. If the tools find nothing, say so.
            Keep answers short and clear.
            """)
    Result<String> chat(@MemoryId String conversationId, @UserMessage String message);
}
