package com.pranit.github.rag.graph;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@Service
public class RepositorySourceWriter {

    public void write(
            Path repositoryRoot,
            String relativePath,
            String content) throws IOException {

        Path target =
                repositoryRoot
                        .resolve(relativePath)
                        .normalize();

        if (!target.startsWith(repositoryRoot)) {
            throw new IllegalArgumentException(
                    "Invalid repository path");
        }

        Files.createDirectories(target.getParent());

        Files.writeString(
                target,
                content,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);
    }
}