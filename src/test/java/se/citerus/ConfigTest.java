package se.citerus;

import org.junit.jupiter.api.Test;
import se.citerus.config.AppConfig;
import se.citerus.config.ConfigHandler;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigTest {
    @Test
    void shouldBeAbleToParsePropertiesFileConfig() {
        AppConfig configuration = ConfigHandler.parseConfigFile(getClass().getResource("/example-configs/config.properties").getPath());
        assertThat(configuration).extracting("githubSignature",
                        "githubEnvironment",
                        "githubOwner",
                        "githubRepository",
                        "githubToken",
                        "port",
                        "deploymentScriptPath")
                .containsExactly("example-value",
                        "example-value",
                        "example-value",
                        "example-value",
                        "example-value",
                        1337,
                        "example-value");
    }

    @Test
    void shouldBeAbleToParseYAMLFileConfig() {
        AppConfig configuration = ConfigHandler.parseConfigFile(getClass().getResource("/example-configs/config.yml").getPath());
        assertThat(configuration).extracting("githubSignature",
                        "githubEnvironment",
                        "githubOwner",
                        "githubRepository",
                        "githubToken",
                        "port",
                        "deploymentScriptPath")
                .containsExactly("example-value",
                        "example-value",
                        "example-value",
                        "example-value",
                        "example-value",
                        1337,
                        "example-value");
    }

    @Test
    void shouldBeAbleToParseJsonFileConfig() {
        AppConfig configuration = ConfigHandler.parseConfigFile(getClass().getResource("/example-configs/config.json").getPath());
        assertThat(configuration).extracting("githubSignature",
                        "githubEnvironment",
                        "githubOwner",
                        "githubRepository",
                        "githubToken",
                        "port",
                        "deploymentScriptPath")
                .containsExactly("example-value",
                        "example-value",
                        "example-value",
                        "example-value",
                        "example-value",
                        1337,
                        "example-value");
    }

    @Test
    void shouldBeAbleToParseXMLFileConfig() {
        AppConfig configuration = ConfigHandler.parseConfigFile(getClass().getResource("/example-configs/config.xml").getPath());
        assertThat(configuration).extracting("githubSignature",
                        "githubEnvironment",
                        "githubOwner",
                        "githubRepository",
                        "githubToken",
                        "port",
                        "deploymentScriptPath")
                .containsExactly("example-value",
                        "example-value",
                        "example-value",
                        "example-value",
                        "example-value",
                        1337,
                        "example-value");
    }
}