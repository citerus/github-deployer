package se.citerus;

import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import org.eclipse.jetty.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class Main {

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);
    private static int deploymentId;

    public static void main(String[] args) throws IOException {
        Optional<String> configFilePath = args.length > 0 && args[0] != null ? Optional.of(args[0]) : Optional.empty();
        Properties props = new Properties();
        props.load(Files.newInputStream(Path.of(configFilePath.orElse("./configuration.properties"))));
        String githubToken = Objects.requireNonNull(props.getProperty("githubToken"), "Missing required config property githubToken.");
        String githubEnvironment = Objects.requireNonNull(props.getProperty("githubEnvironment"), "Missing required config property githubEnvironment.");
        String githubOwner = Objects.requireNonNull(props.getProperty("githubOwner"), "Missing required config property githubOwner.");
        String githubRepository = Objects.requireNonNull(props.getProperty("githubRepository"), "Missing required config property githubRepository.");
        String githubSignature = Objects.requireNonNull(props.getProperty("githubSignature"), "Missing required config property githubSignature.");
        String deploymentScriptPath = Objects.requireNonNull(props.getProperty("deploymentScriptPath"), "Missing required config property deploymentScriptPath.");
        int port = Integer.parseInt(props.getProperty("port", "7070"));

        var ghClient = new GithubRestClient(githubToken, githubEnvironment, githubOwner, githubRepository);
        var app = Javalin.create(/*config*/)
                .post("/webhook", ctx -> {
                    verifySignature(ctx, githubSignature, ctx.header("X-Hub-Signature"));

                    String githubEventType = ctx.header("x-github-event");
                    if (Objects.equals(githubEventType, "ping")) {
                        handlePing(ctx);
                    } else if (Objects.equals(githubEventType, "deployment")) {
                        handleDeployment(ctx, ghClient, githubEnvironment, deploymentScriptPath);
                    } else {
                        LOG.error("Got unhandled event: {}", githubEventType);
                        ctx.status(HttpStatus.BAD_REQUEST);
                    }
                })
                .start(port);
    }

    private static void verifySignature(@NotNull Context ctx, String signature, @Nullable String requestSignature) {
        if (StringUtil.isEmpty(requestSignature)) {
            throw new IllegalStateException("Request signature header was null");
        }
        if (!signature.equals(requestSignature)) {
            LOG.error("Invalid Github signature received: {}", requestSignature);
            ctx.status(HttpStatus.FORBIDDEN);
            throw new IllegalStateException("Invalid Github signature received");
        }
    }

    private static void handleDeployment(@NotNull Context ctx, GithubRestClient ghClient, String githubEnvironment, String deploymentScriptPath) throws IOException, InterruptedException {
        ctx.status(HttpStatus.ACCEPTED).result("Accepted");
        var request = ctx.bodyAsClass(DeploymentRequest.class);
        LOG.info("DeploymentRequest: {}", request);
        if (request.action().equals("created") && request.deployment().environment().equals(githubEnvironment)) {
            LOG.info("Got deployment created request");
            deploymentId = request.deployment().id();
        }

        ghClient.updateDeploymentStatus(DeploymentStatus.in_progress, "Deployment has started", deploymentId);
        try (ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor()) {
            Future<String> future = executorService.submit(() -> runShellScriptFile(deploymentScriptPath));
//            String result = runShellScriptFile(); // TODO run in virtual thread
            future.get(3, TimeUnit.MINUTES);
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
}