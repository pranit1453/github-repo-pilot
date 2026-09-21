package com.pranit.github.rag.graph;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pranit.github.rag.graph.model.CodeGraph;
import com.pranit.github.rag.graph.model.GraphEdge;
import com.pranit.github.rag.graph.model.GraphNode;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

@Service
public class GraphifyJsonParser {

    private final ObjectMapper objectMapper;

    public GraphifyJsonParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public CodeGraph parse(Path graphFile)
            throws IOException {

        JsonNode root =
                objectMapper.readTree(graphFile.toFile());

        List<GraphNode> nodes =
                parseNodes(root);

        List<GraphEdge> edges =
                parseEdges(root);

        Map<String, Object> metadata =
                new HashMap<>();

        root.fields().forEachRemaining(
                entry -> {

                    String name = entry.getKey();

                    if (!name.equals("nodes")
                            && !name.equals("edges")) {

                        metadata.put(
                                name,
                                objectMapper.convertValue(
                                        entry.getValue(),
                                        Object.class));
                    }
                });

        return new CodeGraph(
                nodes,
                edges,
                metadata);
    }

    private List<GraphNode> parseNodes(
            JsonNode root) {

        List<GraphNode> result =
                new ArrayList<>();

        JsonNode nodes =
                root.path("nodes");

        if (!nodes.isArray()) {
            return result;
        }

        for (JsonNode node : nodes) {

            Map<String, Object> properties =
                    new HashMap<>();

            node.fields().forEachRemaining(
                    entry -> {

                        String key = entry.getKey();

                        if (!Set.of(
                                "id",
                                "type",
                                "name",
                                "file",
                                "qualified_name",
                                "qualifiedName"
                        ).contains(key)) {

                            properties.put(
                                    key,
                                    objectMapper.convertValue(
                                            entry.getValue(),
                                            Object.class));
                        }
                    });

            String qualifiedName =
                    text(
                            node,
                            "qualified_name",
                            text(node, "qualifiedName", null));

            result.add(
                    new GraphNode(
                            text(node, "id", null),
                            text(node, "type", "UNKNOWN"),
                            text(node, "name", null),
                            text(node, "file", null),
                            qualifiedName,
                            properties));
        }

        return result;
    }

    private List<GraphEdge> parseEdges(
            JsonNode root) {

        List<GraphEdge> result =
                new ArrayList<>();

        JsonNode edges =
                root.path("edges");

        if (!edges.isArray()) {
            return result;
        }

        for (JsonNode edge : edges) {

            Map<String, Object> properties =
                    new HashMap<>();

            edge.fields().forEachRemaining(
                    entry -> {

                        String key = entry.getKey();

                        if (!Set.of(
                                "source",
                                "target",
                                "type",
                                "relation",
                                "label"
                        ).contains(key)) {

                            properties.put(
                                    key,
                                    objectMapper.convertValue(
                                            entry.getValue(),
                                            Object.class));
                        }
                    });

            String type =
                    text(
                            edge,
                            "type",
                            text(
                                    edge,
                                    "relation",
                                    text(
                                            edge,
                                            "label",
                                            "RELATED_TO")));

            result.add(
                    new GraphEdge(
                            text(edge, "source", null),
                            text(edge, "target", null),
                            type,
                            properties));
        }

        return result;
    }

    private String text(
            JsonNode node,
            String field,
            String fallback) {

        JsonNode value =
                node.get(field);

        return value == null || value.isNull()
                ? fallback
                : value.asText();
    }
}