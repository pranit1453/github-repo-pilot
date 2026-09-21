package com.pranit.github.rag.retrieval;

import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class HybridRetrievalService {

    private final VectorRetrievalService vectorRetrievalService;
    private final GraphNodeResolver graphNodeResolver;
    private final GraphRetrievalService graphRetrievalService;

    public HybridRetrievalService(
            VectorRetrievalService vectorRetrievalService,
            GraphNodeResolver graphNodeResolver,
            GraphRetrievalService graphRetrievalService) {

        this.vectorRetrievalService =
                vectorRetrievalService;

        this.graphNodeResolver =
                graphNodeResolver;

        this.graphRetrievalService =
                graphRetrievalService;
    }

    public HybridContext retrieve(
            UUID repositoryId,
            String question) {

        List<Document> vectorDocuments =
                vectorRetrievalService.search(
                        repositoryId,
                        question,
                        8);

        List<String> nodeIds =
                graphNodeResolver.resolve(
                        vectorDocuments);

        String graphContext =
                graphRetrievalService.expand(
                        nodeIds,
                        2);

        String vectorContext =
                vectorDocuments.stream()
                        .map(Document::getText)
                        .reduce(
                                "",
                                (a, b) ->
                                        a
                                                + "\n\n"
                                                + b);

        return new HybridContext(
                vectorContext,
                graphContext,
                nodeIds);
    }
}