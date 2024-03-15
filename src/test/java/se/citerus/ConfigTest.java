package se.citerus;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.lang3.stream.Streams;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigTest {
    @Test
    void shouldBeAbleToParsePropertiesFileConfig() throws IOException {
        Configuration configuration = Main.parseConfigFile(Optional.of(getClass().getResource("/example-configs/config.properties").getPath()));
        assertThat(configuration.getKeys()).toIterable()
                .containsExactlyInAnyOrder("githubSignature", "githubEnvironment", "githubOwner", "githubRepository", "githubToken", "port", "deploymentScriptPath");
        Map<String, String> entries = Streams.of(configuration.getKeys())
                .map(key -> Map.entry(key, configuration.getString(key)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        assertThat(entries).containsExactlyInAnyOrderEntriesOf(Map.of(
                "githubSignature", "example-value",
                "githubEnvironment", "example-value",
                "githubOwner", "example-value",
                "githubRepository", "example-value",
                "githubToken", "example-value",
                "port", "1337",
                "deploymentScriptPath", "example-value"
        ));
    }

    @Test
    void shouldBeAbleToParseYAMLFileConfig() throws IOException {
        Configuration configuration = Main.parseConfigFile(Optional.of(getClass().getResource("/example-configs/config.yml").getPath()));
        assertThat(configuration.getKeys()).toIterable()
                .containsExactlyInAnyOrder("githubSignature", "githubEnvironment", "githubOwner", "githubRepository", "githubToken", "port", "deploymentScriptPath");
        Map<String, String> entries = Streams.of(configuration.getKeys())
                .map(key -> Map.entry(key, configuration.getString(key)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        assertThat(entries).containsExactlyInAnyOrderEntriesOf(Map.of(
                "githubSignature", "example-value",
                "githubEnvironment", "example-value",
                "githubOwner", "example-value",
                "githubRepository", "example-value",
                "githubToken", "example-value",
                "port", "1337",
                "deploymentScriptPath", "example-value"
        ));
    }

    @Test
    void shouldBeAbleToParseJsonFileConfig() throws IOException {
        Configuration configuration = Main.parseConfigFile(Optional.of(getClass().getResource("/example-configs/config.json").getPath()));
        assertThat(configuration.getKeys()).toIterable()
                .containsExactlyInAnyOrder("githubSignature", "githubEnvironment", "githubOwner", "githubRepository", "githubToken", "port", "deploymentScriptPath");
        Map<String, String> entries = Streams.of(configuration.getKeys())
                .map(key -> Map.entry(key, configuration.getString(key)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        assertThat(entries).containsExactlyInAnyOrderEntriesOf(Map.of(
                "githubSignature", "example-value",
                "githubEnvironment", "example-value",
                "githubOwner", "example-value",
                "githubRepository", "example-value",
                "githubToken", "example-value",
                "port", "1337",
                "deploymentScriptPath", "example-value"
        ));
    }

    @Test
    void shouldBeAbleToParseXMLFileConfig() throws IOException {
        Configuration configuration = Main.parseConfigFile(Optional.of(getClass().getResource("/example-configs/config.xml").getPath()));
        assertThat(configuration.getKeys()).toIterable()
                .containsExactlyInAnyOrder("githubSignature", "githubEnvironment", "githubOwner", "githubRepository", "githubToken", "port", "deploymentScriptPath");
        Map<String, String> entries = Streams.of(configuration.getKeys())
                .map(key -> Map.entry(key, configuration.getString(key)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        assertThat(entries).containsExactlyInAnyOrderEntriesOf(Map.of(
                "githubSignature", "example-value",
                "githubEnvironment", "example-value",
                "githubOwner", "example-value",
                "githubRepository", "example-value",
                "githubToken", "example-value",
                "port", "1337",
                "deploymentScriptPath", "example-value"
        ));
    }
}