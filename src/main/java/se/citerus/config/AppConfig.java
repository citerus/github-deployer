package se.citerus.config;

import org.apache.commons.configuration2.Configuration;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

public class AppConfig {
    private static final String HMAC_SHA_256 = "HmacSHA256";

    public final String githubToken;
    public final String githubEnvironment;
    public final String githubOwner;
    public final String githubRepository;
    public final String githubSignature;
    public final String deploymentScriptPath;
    public final int port;
    public final Mac hmac;

    public AppConfig(Configuration props) {
        // TODO replace Objects.requireNonNull with real validation
        githubToken = Objects.requireNonNull(props.getString("githubToken"), "Missing required config property githubToken.");
        githubEnvironment = Objects.requireNonNull(props.getString("githubEnvironment"), "Missing required config property githubEnvironment.");
        githubOwner = Objects.requireNonNull(props.getString("githubOwner"), "Missing required config property githubOwner.");
        githubRepository = Objects.requireNonNull(props.getString("githubRepository"), "Missing required config property githubRepository.");
        githubSignature = Objects.requireNonNull(props.getString("githubSignature"), "Missing required config property githubSignature.");
        deploymentScriptPath = Objects.requireNonNull(props.getString("deploymentScriptPath"), "Missing required config property deploymentScriptPath.");
        port = props.getInt("port", 7070);
        try {
            hmac = Mac.getInstance(HMAC_SHA_256);
            hmac.init(new SecretKeySpec(githubSignature.getBytes(), HMAC_SHA_256));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Error creating HMAC instance", e);
        }
    }
}
