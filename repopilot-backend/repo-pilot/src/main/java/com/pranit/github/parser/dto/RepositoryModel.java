package com.pranit.github.parser.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record RepositoryModel(
        String owner,
        String repository,
        String branch,
        List<?> files
) {
}
