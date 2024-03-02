package se.citerus;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public class GithubRestClient {
    private static final Logger LOG = LoggerFactory.getLogger(GithubRestClient.class);
    private final String token;
    private final String environment;
    private final String owner;
    private final String repo;

    public GithubRestClient(String token, String environment, String owner, String repo) {
        this.token = token;
        this.environment = environment;
        this.owner = owner;
        this.repo = repo;
    }

    public void updateDeploymentStatus(DeploymentStatus status, String description, int deploymentId) {
        try (HttpClient client = HttpClient.newHttpClient()) {
            String body = new ObjectMapper().writeValueAsString(Map.of(
                    "status", status.name(),
                    "description", description,
                    "environment", environment
            ));
            HttpResponse<String> response = client.send(HttpRequest.newBuilder()
                    .headers("Accept: application/vnd.github+json", "X-GitHub-Api-Version: 2022-11-28", "Authorization: Bearer %s".formatted(token))
                    .uri(URI.create("https://api.github.com/repos/%s/%s/deployments/%s/statuses".formatted(owner, repo, deploymentId)))
                    .POST(HttpRequest.BodyPublishers.ofString(body)).build(), HttpResponse.BodyHandlers.ofString());
            LOG.info("Deployment status update response: {}", response.statusCode());
            LOG.debug("Deployment status update response body: {}", response.body());
        } catch (IOException | InterruptedException e) {
//            throw new RuntimeException(e);
            LOG.error("Failed to update deployment status", e);
        }
    }
}
