package com.pranit.github.rag.embedding;

public record GraphEmbeddingDocument(
        String nodeId,
        String repositoryId,
        String filePath,
        String content,
        String graphContext) {

    public String embeddingText() {

        return """
                Repository: %s
                File: %s
                Graph Node: %s

                SOURCE CODE:
                %s

                GRAPH CONTEXT:
                %s
                """.formatted(
                repositoryId,
                filePath,
                nodeId,
                content,
                graphContext);
    }
}