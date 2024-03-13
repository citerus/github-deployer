package se.citerus;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.HttpStatus;
import org.eclipse.jetty.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.citerus.clients.GithubRestClient;
import se.citerus.model.DeploymentRequest;
import se.citerus.model.DeploymentStatus;
import se.citerus.model.PingRequest;

import javax.crypto.Mac;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.stream.Collectors;

class WebhookHandler implements Handler {
    private static final Logger LOG = LoggerFactory.getLogger(WebhookHandler.class);
    private final GithubRestClient ghClient;
    private final String githubEnvironment;
    private final String deploymentScriptPath;
    private final Mac hmac;
    private static int deploymentId;
    private static final String HEXES = "0123456789abcdef";

    public WebhookHandler(GithubRestClient ghClient, String githubEnvironment, String deploymentScriptPath, Mac hmac) {
        this.ghClient = ghClient;
        this.githubEnvironment = githubEnvironment;
        this.deploymentScriptPath = deploymentScriptPath;
        this.hmac = hmac;
    }

    @Override
    public void handle(@NotNull Context ctx) throws Exception {
//                    verifySignature(ctx, githubSignature, ctx.header("X-Hub-Signature"));
        verifySignature(ctx, ctx.header("X-Hub-Signature-256"), hmac);

        String githubEventType = ctx.header("x-github-event");
        if (Objects.equals(githubEventType, "ping")) {
            handlePing(ctx);
        } else if (Objects.equals(githubEventType, "deployment")) {
            handleDeployment(ctx, ghClient, githubEnvironment, deploymentScriptPath);
        } else {
            LOG.error("Got unhandled event: {}", githubEventType);
            ctx.status(HttpStatus.BAD_REQUEST);
        }
    }

    private static void verifySignature(@NotNull Context ctx, @Nullable String requestSignature, @NotNull Mac hmac) {
        if (StringUtil.isEmpty(requestSignature)) {
            throw new IllegalStateException("Request signature header was null");
        }
        final String digestHex = "sha256=" + getHex(hmac.doFinal(ctx.bodyAsBytes()));
        if (!digestHex.equals(requestSignature)) {
            LOG.error("Invalid Github signature received: {}", requestSignature);
            ctx.status(HttpStatus.FORBIDDEN);
            throw new IllegalStateException("Invalid Github signature received");
        }
    }

    private static void handleDeployment(@NotNull Context ctx, GithubRestClient ghClient, String githubEnvironment, String deploymentScriptPath) throws InterruptedException {
        ctx.status(HttpStatus.ACCEPTED).result("Accepted");
        var request = ctx.bodyAsClass(DeploymentRequest.class);
        LOG.info("DeploymentRequest: {}", request);
        if (request.action().equals("created") && request.deployment().environment().equals(githubEnvironment)) {
            LOG.info("Got deployment created request with id {}", request.deployment().id());
            deploymentId = request.deployment().id();
        } else {
            throw new IllegalStateException("Request environment did not match configured environment: %s vs %s".formatted(request.deployment().environment(), githubEnvironment));
        }

        ghClient.updateDeploymentStatus(DeploymentStatus.in_progress, "Deployment has started", deploymentId);
        try (ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor()) {
            Future<String> future = executorService.submit(() -> runShellScriptFile(deploymentScriptPath));
            String output = future.get(3, TimeUnit.MINUTES);
            // TODO store output in file and make retrievable through /logs/{deployment_id} endpoint
            LOG.info(output);
            ghClient.updateDeploymentStatus(DeploymentStatus.success, "Successfully deployed", deploymentId);
        } catch (IllegalStateException | ExecutionException e) {
            LOG.error("Error running deployment script", e);
            ghClient.updateDeploymentStatus(DeploymentStatus.failure, "Error running deployment script: %s".formatted(e), deploymentId);
        } catch (TimeoutException e) {
            LOG.error("Timed out waiting for deployment script", e);
            ghClient.updateDeploymentStatus(DeploymentStatus.failure, "Timed out waiting for deployment script: %s".formatted(e), deploymentId);
        }
    }

    private static void handlePing(@NotNull Context ctx) {
        ctx.status(HttpStatus.ACCEPTED).result("Accepted");
        var request = ctx.bodyAsClass(PingRequest.class);
        LOG.debug("PingRequest: {}", request);
    }

    private static String runShellScriptFile(String filePath) {
        try {
            Process process = new ProcessBuilder("/bin/sh", filePath).redirectErrorStream(true).start();
            int result = process.waitFor();
            LOG.info("Process exit code: {}", result);
            return readOutput(process.getInputStream());
        } catch (IOException | InterruptedException e) {
            throw new IllegalStateException("Deployment script error", e);
        }
    }

    private static String readOutput(InputStream inputStream) throws IOException {
        return new BufferedReader(new InputStreamReader(inputStream)).lines().collect(Collectors.joining("\n"));
    }

    private static String getHex(byte[] raw) {
        final StringBuilder hex = new StringBuilder(2 * raw.length);
        for (final byte b : raw) {
            hex.append(HEXES.charAt((b & 0xF0) >> 4)).append(HEXES.charAt((b & 0x0F)));
        }
        return hex.toString();
    }
}
