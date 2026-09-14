package com.pranit.github.client;

import java.util.List;
import java.util.Map;

public interface GitHubApiClient {

    List<Map<String, Object>> listUserRepos(String accessToken);

    Map<String, Object> getRepoTree(String accessToken, String owner, String repo, String branch);

    String getFileContent(String accessToken, String owner, String repo, String path);
}
