package com.pranit.github.rag.graph;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

@Service
public class RepositorySourceLoader {

    public Map<String, String> load(
            Path sourceDirectory) throws IOException {

        Map<String, String> result =
                new HashMap<>();

        try (var stream =
                     Files.walk(sourceDirectory)) {

            stream.filter(Files::isRegularFile)
                    .forEach(file -> {

                        try {

                            String relative =
                                    sourceDirectory
                                            .relativize(file)
                                            .toString()
                                            .replace(
                                                    java.nio.file.FileSystems
                                                            .getDefault()
                                                            .getSeparator(),
                                                    "/");

                            result.put(
                                    relative,
                                    Files.readString(file));

                        } catch (IOException e) {

                            throw new RuntimeException(e);
                        }
                    });
        }

        return result;
    }
}