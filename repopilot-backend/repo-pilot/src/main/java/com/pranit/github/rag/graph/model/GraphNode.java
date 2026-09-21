package com.pranit.github.rag.graph.model;

import java.util.Map;

public record GraphNode(
        String id,
        String type,
        String name,
        String file,
        String qualifiedName,
        Map<String, Object> properties) {
}