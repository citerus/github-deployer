package se.citerus;

import io.javalin.Javalin;
import io.javalin.http.Context;
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
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public class Main {

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);
    private static final String HMAC_SHA_256 = "HmacSHA256";

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        // TODO move config handling to separate class and implement yaml and json support
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
        Mac hmac = Mac.getInstance(HMAC_SHA_256);
        hmac.init(new SecretKeySpec(githubSignature.getBytes(), HMAC_SHA_256));

        // TODO create deployment pipeline with manual trigger and test it
        var ghClient = new GithubRestClient(githubToken, githubEnvironment, githubOwner, githubRepository);
        var app = Javalin.create(/*config*/)
                .post("/webhook", new WebhookHandler(ghClient, githubEnvironment, deploymentScriptPath, hmac))
                .get("/logs/{deploymentId}", new LogsHandler())
                .start(port);
    }
}