package com.pranit.github.rag.graph.model;

import java.util.Map;

public record GraphEdge(
        String source,
        String target,
        String type,
        Map<String, Object> properties) {
}