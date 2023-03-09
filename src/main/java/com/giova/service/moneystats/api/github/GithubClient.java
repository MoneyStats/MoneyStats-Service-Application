package com.giova.service.moneystats.api.github;

import com.giova.service.moneystats.app.model.GithubIssues;
import io.github.giovannilamarmora.utils.interceptors.Logged;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Logged
public class GithubClient {

  @Value(value = "${rest.client.github.issuesUrl}")
  private String openGithubIssuesUrl;

  @Value(value = "${rest.client.github.authToken}")
  private String githubAuthToken;

  private final RestTemplate restTemplate = new RestTemplate();

  public ResponseEntity<Object> openGithubIssues(GithubIssues githubIssues) {
    HttpHeaders headers = new HttpHeaders();
    headers.add("Accept", "application/vnd.github+json");
    headers.add("Authorization", "Bearer " + githubAuthToken);
    headers.add("X-GitHub-Api-Version", "2022-11-28");
    HttpEntity<GithubIssues> request = new HttpEntity<>(githubIssues, headers);

    ResponseEntity<Object> response =
        restTemplate.exchange(openGithubIssuesUrl, HttpMethod.POST, request, Object.class);

    return response;
  }
}
