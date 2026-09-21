package com.pranit.github.rag.retrieval;

import java.util.List;

public record HybridContext(
        String vectorContext,
        String graphContext,
        List<String> nodeIds) {
}