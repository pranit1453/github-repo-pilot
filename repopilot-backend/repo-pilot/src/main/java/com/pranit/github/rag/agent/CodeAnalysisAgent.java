package com.pranit.github.rag.agent;

import com.pranit.github.rag.retrieval.HybridContext;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class CodeAnalysisAgent {

    private final ChatClient chatClient;

    public CodeAnalysisAgent(
            ChatClient.Builder chatClientBuilder) {

        this.chatClient =
                chatClientBuilder.build();
    }

    public String answer(
            String question,
            HybridContext context) {

        String prompt = """
                You are a software code analysis assistant.

                Answer the user's question using only
                the supplied repository context.

                VECTOR CONTEXT:
                %s

                KNOWLEDGE GRAPH CONTEXT:
                %s

                RULES:
                - Prefer explicit source evidence.
                - Use graph relationships to explain dependencies.
                - Do not invent classes or relationships.
                - Mention file and symbol names when available.
                - Explain the execution or dependency flow clearly.

                USER QUESTION:
                %s
                """.formatted(
                context.vectorContext(),
                context.graphContext(),
                question);

        return chatClient
                .prompt()
                .user(prompt)
                .call()
                .content();
    }
}