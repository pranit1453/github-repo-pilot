package com.pranit.github.rag.embedding;

import com.pranit.github.rag.graph.model.CodeGraph;
import com.pranit.github.rag.graph.model.GraphEdge;
import com.pranit.github.rag.graph.model.GraphNode;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class GraphAwareVectorIndexingService {

    private final VectorStore vectorStore;

    public GraphAwareVectorIndexingService(
            VectorStore vectorStore) {

        this.vectorStore = vectorStore;
    }

    public void index(
            UUID repositoryId,
            CodeGraph graph,
            Map<String, String> sourceFiles) {

        Map<String, List<GraphEdge>> edgesByNode =
                graph.edges()
                        .stream()
                        .collect(
                                Collectors.groupingBy(
                                        GraphEdge::source));

        List<Document> documents =
                new ArrayList<>();

        for (GraphNode node : graph.nodes()) {

            if (node.file() == null) {
                continue;
            }

            String source =
                    sourceFiles.get(node.file());

            if (source == null) {
                continue;
            }

            List<GraphEdge> edges =
                    edgesByNode.getOrDefault(
                            node.id(),
                            List.of());

            String graphContext =
                    buildGraphContext(
                            node,
                            edges,
                            graph);

            String text = """
                    Repository: %s
                    File: %s
                    Symbol: %s
                    Type: %s
                    Qualified Name: %s

                    Source:
                    %s

                    Relationships:
                    %s
                    """.formatted(
                    repositoryId,
                    node.file(),
                    node.name(),
                    node.type(),
                    node.qualifiedName(),
                    source,
                    graphContext);

            Map<String, Object> metadata =
                    new HashMap<>();

            metadata.put(
                    "repositoryId",
                    repositoryId.toString());

            metadata.put(
                    "nodeId",
                    node.id());

            metadata.put(
                    "nodeType",
                    node.type());

            metadata.put(
                    "symbol",
                    node.name());

            metadata.put(
                    "filePath",
                    node.file());

            metadata.put(
                    "qualifiedName",
                    node.qualifiedName());

            documents.add(
                    new Document(
                            text,
                            metadata));
        }

        if (!documents.isEmpty()) {
            vectorStore.add(documents);
        }
    }

    private String buildGraphContext(
            GraphNode node,
            List<GraphEdge> outgoing,
            CodeGraph graph) {

        Map<String, GraphNode> nodeMap =
                graph.nodes()
                        .stream()
                        .collect(
                                Collectors.toMap(
                                        GraphNode::id,
                                        n -> n,
                                        (a, b) -> a));

        StringBuilder result =
                new StringBuilder();

        for (GraphEdge edge : outgoing) {

            GraphNode target =
                    nodeMap.get(edge.target());

            result.append(edge.type())
                    .append(" -> ");

            if (target != null) {
                result.append(target.qualifiedName());
            } else {
                result.append(edge.target());
            }

            result.append("\n");
        }

        return result.toString();
    }
}