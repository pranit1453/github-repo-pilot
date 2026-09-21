package com.pranit.github.rag.retrieval;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class VectorRetrievalService {

    private final VectorStore vectorStore;

    public VectorRetrievalService(
            VectorStore vectorStore) {

        this.vectorStore = vectorStore;
    }

    public List<Document> search(
            UUID repositoryId,
            String query,
            int topK) {

        return vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(query)
                        .topK(topK)
                        .filterExpression(
                                "repositoryId == '"
                                        + repositoryId
                                        + "'")
                        .build());
    }
}