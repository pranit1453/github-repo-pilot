package com.pranit.github.rag.graph;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class GraphifyWorkspaceService {

    private final Path root;

    public GraphifyWorkspaceService(
            @Value("${app.graphify.workspace}") String root) {

        this.root = Paths.get(root);
    }

    public Path create(UUID repositoryId) throws IOException {

        Path repositoryRoot =
                root.resolve(repositoryId.toString());

        Path source =
                repositoryRoot.resolve("source");

        Files.createDirectories(source);

        return source;
    }

    public Path output(Path sourceDirectory) {

        return sourceDirectory
                .getParent()
                .resolve("graphify-out");
    }
}