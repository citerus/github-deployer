package se.citerus.model;

import java.util.Map;

public record Deployment(
        String created_at,
        Map<String, ?> creator,
        String description,
        String environment,
        Integer id,
        String node_id,
        String original_environment,
        String payload,
        Map<String, ?> performed_via_github_app,
        Boolean production_environment,
        String ref,
        String repository_url,
        String sha,
        String statuses_url,
        String task,
        Boolean transient_environment,
        String updated_at,
        String url
) {
}
