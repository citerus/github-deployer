package se.citerus.model;

import java.util.List;
import java.util.Map;

public record PingRequest(Hook hook, int hook_id, Map<String, ?> organization, Map<String, ?> repository, Map<String, ?> sender, String zen) {
}

record Hook(boolean active, Integer app_id, HookConfig config, String created_at, String deliveries_url,
            List<String> events,
            int id, PingResponse last_response, String name, String ping_url, String test_url, String type,
            String updated_at,
            String url) {
}

record HookConfig(String content_type, String insecure_ssl, String secret, String url) {
}

record PingResponse(Integer code, String status, String message) {
}