package se.citerus;

import io.javalin.Javalin;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.JSONConfiguration;
import org.apache.commons.configuration2.YAMLConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.citerus.clients.GithubRestClient;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.Optional;

public class Main {

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);
    private static final String HMAC_SHA_256 = "HmacSHA256";

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        Optional<String> configFilePath = args.length > 0 && args[0] != null ? Optional.of(args[0]) : Optional.empty();
        Configuration props = parseConfigFile(configFilePath);
        String githubToken = Objects.requireNonNull(props.getString("githubToken"), "Missing required config property githubToken.");
        String githubEnvironment = Objects.requireNonNull(props.getString("githubEnvironment"), "Missing required config property githubEnvironment.");
        String githubOwner = Objects.requireNonNull(props.getString("githubOwner"), "Missing required config property githubOwner.");
        String githubRepository = Objects.requireNonNull(props.getString("githubRepository"), "Missing required config property githubRepository.");
        String githubSignature = Objects.requireNonNull(props.getString("githubSignature"), "Missing required config property githubSignature.");
        String deploymentScriptPath = Objects.requireNonNull(props.getString("deploymentScriptPath"), "Missing required config property deploymentScriptPath.");
        int port = props.getInt("port", 7070);
        Mac hmac = Mac.getInstance(HMAC_SHA_256);
        hmac.init(new SecretKeySpec(githubSignature.getBytes(), HMAC_SHA_256));

        // TODO create deployment pipeline with manual trigger and test it
        var ghClient = new GithubRestClient(githubToken, githubEnvironment, githubOwner, githubRepository);
        var app = Javalin.create(/*config*/)
                .post("/webhook", new WebhookHandler(ghClient, githubEnvironment, deploymentScriptPath, hmac))
                .get("/logs/{deploymentId}", new LogsHandler())
                .start(port);
    }

    @NotNull
    protected static Configuration parseConfigFile(Optional<String> configFilePath) throws IOException {
        try {
            Configuration config;
            Configurations configs = new Configurations();
            if (configFilePath.isPresent()) {
                String filePath = configFilePath.get();
                String fileExtension = extractFileExtension(filePath);
                switch (fileExtension) {
                    case ".json" -> {
                        JSONConfiguration jsonConfiguration = new JSONConfiguration();
                        jsonConfiguration.read(Files.newInputStream(Path.of(filePath)));
                        config = jsonConfiguration;
                    }
                    case ".yml", ".yaml" -> {
                        YAMLConfiguration yamlConfiguration = new YAMLConfiguration();
                        yamlConfiguration.read(Files.newInputStream(Path.of(filePath)));
                        config = yamlConfiguration;
                    }
                    case ".xml" -> config = configs.xml(filePath);
                    case ".properties" -> config = configs.properties(filePath);
                    default -> throw new IllegalArgumentException("Unknown config file format: %s".formatted(fileExtension));
                }
            } else if (Files.exists(Path.of("configuration.properties"))) {
                config = configs.properties(new File("configuration.properties"));
            } else if (Files.exists(Path.of("configuration.json"))) {
                JSONConfiguration jsonConfiguration = new JSONConfiguration();
                jsonConfiguration.read(Files.newInputStream(Path.of("configuration.json")));
                config = jsonConfiguration;
            } else if (Files.exists(Path.of("configuration.yml"))) {
                YAMLConfiguration yamlConfiguration = new YAMLConfiguration();
                yamlConfiguration.read(Files.newInputStream(Path.of("configuration.yml")));
                config = yamlConfiguration;
            } else if (Files.exists(Path.of("configuration.xml"))) {
                config = configs.xml("configuration.xml");
            } else {
                LOG.error("Unknown config file format: {}", configFilePath.orElse("missing"));
                throw new IllegalArgumentException("Unknown config file format: %s".formatted(configFilePath.orElse("missing")));
            }
            return config;
        } catch (ConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    private static String extractFileExtension(String configFilePath) {
        String substring = configFilePath.substring(configFilePath.lastIndexOf("."));
        if (substring.isEmpty()) {
            throw new IllegalArgumentException("Config file path is missing a file extension: %s".formatted(configFilePath));
        }
        return substring;
    }
}