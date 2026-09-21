package com.pranit.github.rag.graph;

import com.pranit.github.rag.graph.model.CodeGraph;
import com.pranit.github.rag.graph.model.GraphEdge;
import com.pranit.github.rag.graph.model.GraphNode;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class GraphContextBuilder {

    public String build(
            CodeGraph graph,
            Set<String> nodeIds) {

        Map<String, GraphNode> nodeMap =
                graph.nodes()
                        .stream()
                        .collect(
                                Collectors.toMap(
                                        GraphNode::id,
                                        n -> n,
                                        (a, b) -> a));

        List<GraphEdge> relevantEdges =
                graph.edges()
                        .stream()
                        .filter(edge ->
                                nodeIds.contains(edge.source())
                                        || nodeIds.contains(edge.target()))
                        .toList();

        Set<String> relatedIds =
                new HashSet<>(nodeIds);

        relevantEdges.forEach(edge -> {
            relatedIds.add(edge.source());
            relatedIds.add(edge.target());
        });

        StringBuilder context =
                new StringBuilder();

        context.append("CODE KNOWLEDGE GRAPH\n\n");

        for (String nodeId : relatedIds) {

            GraphNode node =
                    nodeMap.get(nodeId);

            if (node == null) {
                continue;
            }

            context.append("NODE\n");
            context.append("id: ")
                    .append(node.id())
                    .append("\n");

            context.append("type: ")
                    .append(node.type())
                    .append("\n");

            context.append("name: ")
                    .append(node.name())
                    .append("\n");

            context.append("file: ")
                    .append(node.file())
                    .append("\n");

            context.append("qualifiedName: ")
                    .append(node.qualifiedName())
                    .append("\n\n");
        }

        context.append("RELATIONSHIPS\n");

        for (GraphEdge edge : relevantEdges) {

            context.append(edge.source())
                    .append(" --")
                    .append(edge.type())
                    .append("--> ")
                    .append(edge.target())
                    .append("\n");
        }

        return context.toString();
    }
}