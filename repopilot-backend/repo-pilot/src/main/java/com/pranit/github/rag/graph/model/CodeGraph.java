package com.pranit.github.rag.graph.model;

import java.util.List;
import java.util.Map;

public record CodeGraph(
        List<GraphNode> nodes,
        List<GraphEdge> edges,
        Map<String, Object> metadata) {
}