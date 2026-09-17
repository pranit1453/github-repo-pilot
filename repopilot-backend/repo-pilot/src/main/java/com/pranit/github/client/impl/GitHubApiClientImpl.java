package com.pranit.github.client.impl;

import com.pranit.github.client.GitHubApiClient;
import com.pranit.github.constant.Client;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public final class GitHubApiClientImpl implements GitHubApiClient {

    private static final int MAX_PAGE_SIZE = 100;

    private static final ParameterizedTypeReference<List<Map<String, Object>>> REPOSITORY_LIST_RESPONSE = new ParameterizedTypeReference<>() {
    };
    private static final ParameterizedTypeReference<Map<String, Object>> GITHUB_OBJECT_RESPONSE = new ParameterizedTypeReference<>() {
    };

    private final RestClient.Builder restClient;

    @Override
    public List<Map<String, Object>> listUserRepos(final String accessToken) {
        final List<Map<String, Object>> allRepositories = new ArrayList<>();
        int page = 1;
        while (true) {
            final int currentPage = page;
            List<Map<String, Object>> repositories = client(accessToken)
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/user/repos")
                            .queryParam("affiliation", "owner,collaborator,organization_member")
                            .queryParam("sort", "updated")
                            .queryParam("per_page", MAX_PAGE_SIZE)
                            .queryParam("page", currentPage)
                            .build())
                    .retrieve()
                    .body(REPOSITORY_LIST_RESPONSE);

            if (repositories == null || repositories.isEmpty()) break;
            allRepositories.addAll(repositories);
            if (repositories.size() < MAX_PAGE_SIZE) break;
            page++;
        }
        return allRepositories;
    }

    @Override
    public Map<String, Object> getRepoTree(String accessToken, String owner, String repo, String branch) {
        return client(accessToken)
                .get()
                .uri("/repos/{owner}/{repo}/git/trees/{branch}?recursive=1", owner, repo, branch)
                .retrieve()
                .body(GITHUB_OBJECT_RESPONSE);
    }

    @Override
    public String getFileContent(String accessToken, String owner, String repo, String path) {
        final Map<String, Object> response = client(accessToken)
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/repos/{owner}/{repo}/contents/{+path}")
                        .build(owner, repo, path))
                .retrieve()
                .body(GITHUB_OBJECT_RESPONSE);
        if (response == null) return null;
        Object encoding = response.get("encoding");
        Object content = response.get("content");
        if (content == null) return null;
        if ("base64".equalsIgnoreCase(String.valueOf(encoding))) {
            final String encodedContent = String.valueOf(content).replaceAll("\\s", "");
            final byte[] decodedContent = Base64.getDecoder().decode(encodedContent);
            return new String(decodedContent, StandardCharsets.UTF_8);
        }
        return String.valueOf(content);
    }

    private RestClient client(final String accessToken) {
        return restClient
                .baseUrl(Client.API_BASE_URL)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .defaultHeader(HttpHeaders.ACCEPT, "application/vnd.github+json")
                .defaultHeader("X-GitHub-Api-Version", "2022-11-28")
                .defaultHeader(HttpHeaders.USER_AGENT, "RepoPilot")
                .build();
    }
}
