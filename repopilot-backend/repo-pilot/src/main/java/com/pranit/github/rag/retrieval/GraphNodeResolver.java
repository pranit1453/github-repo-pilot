package com.pranit.github.rag.retrieval;

import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GraphNodeResolver {

    public List<String> resolve(
            List<Document> documents) {

        return documents.stream()
                .map(document ->
                        document.getMetadata()
                                .get("nodeId"))
                .filter(
                        nodeId ->
                                nodeId != null)
                .map(Object::toString)
                .distinct()
                .toList();
    }
}