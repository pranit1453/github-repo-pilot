package com.pranit.github.rag.retrieval;

import com.fasterxml.jackson.databind.JsonNode;
import com.pranit.github.rag.graph.GraphifyMcpClient;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GraphRetrievalService {

    private final GraphifyMcpClient graphify;

    public GraphRetrievalService(
            GraphifyMcpClient graphify) {

        this.graphify = graphify;
    }

    public String expand(
            List<String> nodeIds,
            int depth) {

        StringBuilder context =
                new StringBuilder();

        for (String nodeId : nodeIds) {

            JsonNode result =
                    graphify.callTool(
                            "get_node",
                            Map.of(
                                    "node_id",
                                    nodeId));

            context.append(
                    result.toPrettyString())
                    .append("\n");

            JsonNode neighbors =
                    graphify.callTool(
                            "get_neighbors",
                            Map.of(
                                    "node_id",
                                    nodeId));

            context.append(
                    neighbors.toPrettyString())
                    .append("\n");
        }

        return context.toString();
    }
}