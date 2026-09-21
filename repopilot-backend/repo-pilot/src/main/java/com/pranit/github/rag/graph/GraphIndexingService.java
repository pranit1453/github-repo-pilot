package com.pranit.github.rag.graph;

import com.pranit.github.rag.embedding.GraphAwareVectorIndexingService;
import com.pranit.github.rag.graph.model.CodeGraph;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

@Service
public class GraphIndexingService {

    private final GraphifyProcessService graphifyProcessService;
    private final GraphifyJsonParser graphifyJsonParser;
    private final RepositorySourceLoader sourceLoader;
    private final GraphAwareVectorIndexingService vectorIndexingService;

    public GraphIndexingService(
            GraphifyProcessService graphifyProcessService,
            GraphifyJsonParser graphifyJsonParser,
            RepositorySourceLoader sourceLoader,
            GraphAwareVectorIndexingService vectorIndexingService) {

        this.graphifyProcessService =
                graphifyProcessService;

        this.graphifyJsonParser =
                graphifyJsonParser;

        this.sourceLoader =
                sourceLoader;

        this.vectorIndexingService =
                vectorIndexingService;
    }

    public void buildAndIndex(
            UUID repositoryId,
            Path sourceDirectory)
            throws Exception {

        Path graphFile =
                graphifyProcessService
                        .build(sourceDirectory);

        CodeGraph graph =
                graphifyJsonParser
                        .parse(graphFile);

        Map<String, String> sourceFiles =
                sourceLoader.load(sourceDirectory);

        vectorIndexingService.index(
                repositoryId,
                graph,
                sourceFiles);
    }
}