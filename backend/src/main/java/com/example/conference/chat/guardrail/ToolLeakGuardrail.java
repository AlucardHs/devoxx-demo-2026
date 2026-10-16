package com.example.conference.chat.guardrail;

import com.example.conference.chat.ConferenceTools;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.guardrail.OutputGuardrail;
import dev.langchain4j.guardrail.OutputGuardrailResult;
import jakarta.inject.Singleton;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * Last line of defence: blocks any answer that mentions a tool name or talks about its own instructions,
 * in case an attack got past the input guardrails.
 */
@Singleton
public class ToolLeakGuardrail implements OutputGuardrail {

    private static final List<String> FORBIDDEN = Stream.concat(
            Arrays.stream(ConferenceTools.class.getMethods())
                    .filter(method -> method.isAnnotationPresent(Tool.class))
                    .map(method -> method.getName().toLowerCase()),
            Stream.of("system prompt", "system message", "my instructions"))
            .toList();

    @Override
    public OutputGuardrailResult validate(AiMessage responseFromLLM) {
        String reply = responseFromLLM.text() == null ? "" : responseFromLLM.text().toLowerCase();
        return FORBIDDEN.stream()
                .filter(reply::contains)
                .findFirst()
                .map(term -> failureWithMessageRemoval("The answer reveals internal details: " + term))
                .orElseGet(this::success);
    }
}
