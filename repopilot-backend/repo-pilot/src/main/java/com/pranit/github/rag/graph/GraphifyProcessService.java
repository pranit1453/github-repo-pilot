package com.pranit.github.rag.graph;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class GraphifyProcessService {

    private final String executable;
    private final long timeoutSeconds;

    public GraphifyProcessService(
            @Value("${app.graphify.executable:graphify}")
            String executable,
            @Value("${app.graphify.timeout-seconds:300}")
            long timeoutSeconds) {

        this.executable = executable;
        this.timeoutSeconds = timeoutSeconds;
    }

    public Path build(Path sourceDirectory)
            throws IOException, InterruptedException {

        ProcessBuilder builder =
                new ProcessBuilder(
                        executable,
                        "extract",
                        sourceDirectory.toString(),
                        "--code-only");

        builder.redirectErrorStream(true);

        Process process = builder.start();

        try (BufferedReader reader =
                     new BufferedReader(
                             new InputStreamReader(
                                     process.getInputStream()))) {

            String line;

            while ((line = reader.readLine()) != null) {
                log.info("[graphify] {}", line);
            }
        }

        boolean completed =
                process.waitFor(
                        timeoutSeconds,
                        TimeUnit.SECONDS);

        if (!completed) {
            process.destroyForcibly();

            throw new IllegalStateException(
                    "Graphify timed out");
        }

        if (process.exitValue() != 0) {
            throw new IllegalStateException(
                    "Graphify failed: "
                            + process.exitValue());
        }

        Path output =
                sourceDirectory
                        .resolve("graphify-out")
                        .resolve("graph.json");

        if (!Files.exists(output)) {
            throw new IllegalStateException(
                    "graph.json was not generated");
        }

        return output;
    }
}