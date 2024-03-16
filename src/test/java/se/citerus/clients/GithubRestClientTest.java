package se.citerus.clients;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.github.tomakehurst.wiremock.matching.UrlPathPattern;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.MapConfiguration;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.ReflectionUtils;
import se.citerus.clients.GithubRestClient;
import se.citerus.config.AppConfig;
import se.citerus.model.DeploymentStatus;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@WireMockTest
class GithubRestClientTest {
    @Test
    void shouldUpdateDeploymentSuccessfully(WireMockRuntimeInfo wm) throws Exception {
        Configuration config = new MapConfiguration(Map.of(
                "githubToken", "test",
                "githubEnvironment", "test",
                "githubOwner", "testOwner",
                "githubRepository", "testRepo",
                "githubSignature", "test",
                "deploymentScriptPath", "test"
        ));
        GithubRestClient githubRestClient = new GithubRestClient(new AppConfig(config));
        List<Field> fields = ReflectionUtils.findFields(GithubRestClient.class, (f) -> f.getName().equals("host"), ReflectionUtils.HierarchyTraversalMode.TOP_DOWN);
        fields.getFirst().setAccessible(true);
        fields.getFirst().set(githubRestClient, "http://localhost:%s".formatted(wm.getHttpPort()));
        stubFor(post("/repos/testOwner/testRepo/deployments/1/statuses").willReturn(ok("OK")));

        githubRestClient.updateDeploymentStatus(DeploymentStatus.queued, "test", 1);

        verify(postRequestedFor(urlPathEqualTo("/repos/testOwner/testRepo/deployments/1/statuses")));
    }
}