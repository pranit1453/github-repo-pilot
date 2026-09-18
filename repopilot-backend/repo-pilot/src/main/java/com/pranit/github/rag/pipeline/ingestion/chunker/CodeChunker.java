package com.pranit.github.rag.pipeline.ingestion.chunker;

import com.pranit.github.constant.RepoMetadata;
import com.pranit.github.properties.RagProperties;
import com.pranit.github.rag.pipeline.ingestion.filter.CodeFileFilter;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@Component
public class CodeChunker {

    private final TokenTextSplitter splitter;
    private final CodeFileFilter codeFileFilter;

    public CodeChunker(RagProperties properties, CodeFileFilter codeFileFilter) {
        final var config = properties.chunking();
        int chunkTokens = Math.max(50, config.chunkSize() / 4);
        this.splitter = TokenTextSplitter.builder()
                .withChunkSize(chunkTokens)
                .withMinChunkSizeChars(config.minChunkSizeChars())
                .withMinChunkLengthToEmbed(config.minChunkLengthToEmbed())
                .withMaxNumChunks(config.maxNumChunks())
                .withKeepSeparator(config.keepSeparator())
                .build();
        this.codeFileFilter = codeFileFilter;
    }

    private static Map<String, Object> baseMetadata(final String repositoryId, final String filePath, final String language) {
        final Map<String, Object> metadata = new HashMap<>();
        metadata.put(RepoMetadata.METADATA_REPO_ID, repositoryId);
        metadata.put("filePath", filePath);
        metadata.put("language", language);
        return metadata;
    }

    private static Document withChunkIndex(
            final Document chunk, final String repositoryId, final String filePath,
            final String language, final int chunkIndex) {
        final Map<String, Object> metadata = new HashMap<>(chunk.getMetadata());
        metadata.put(RepoMetadata.METADATA_REPO_ID, repositoryId);
        metadata.put("filePath", filePath);
        metadata.put("language", language);
        metadata.put("chunkIndex", chunkIndex);
        return new Document(chunk.getText(), metadata);
    }

    public List<Document> chunkFile(final String repositoryId, final String filePath, final String content) {
        if (content == null || content.isBlank()) return List.of();
        final String language = codeFileFilter.detectLanguage(filePath);
        final String header = "// File: " + filePath + "\n";
        final Document source = new Document(header + content, baseMetadata(repositoryId, filePath, language));
        final List<Document> split = splitter.apply(List.of(source));
        return IntStream.range(0, split.size())
                .mapToObj(i -> withChunkIndex(split.get(i), repositoryId, filePath, language, i))
                .toList();
    }
}
