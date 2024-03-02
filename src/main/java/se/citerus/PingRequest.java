package se.citerus;

import java.util.List;

public record PingRequest(Hook hook, int hook_id, String organization, String repository, String sender, String zen) {
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