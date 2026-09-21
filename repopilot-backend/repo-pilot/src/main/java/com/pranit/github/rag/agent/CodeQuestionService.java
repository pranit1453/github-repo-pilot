package com.pranit.github.rag.agent;

import com.pranit.github.rag.retrieval.HybridContext;
import com.pranit.github.rag.retrieval.HybridRetrievalService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CodeQuestionService {

    private final HybridRetrievalService retrievalService;
    private final CodeAnalysisAgent agent;

    public CodeQuestionService(
            HybridRetrievalService retrievalService,
            CodeAnalysisAgent agent) {

        this.retrievalService =
                retrievalService;

        this.agent =
                agent;
    }

    public String ask(
            UUID repositoryId,
            String question) {

        HybridContext context =
                retrievalService.retrieve(
                        repositoryId,
                        question);

        return agent.answer(
                question,
                context);
    }
}