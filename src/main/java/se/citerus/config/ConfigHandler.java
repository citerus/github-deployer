package se.citerus.config;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.JSONConfiguration;
import org.apache.commons.configuration2.YAMLConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigHandler {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigHandler.class);
    public static final String PROPERTIES_FILE_DEFAULT_NAME = "configuration.properties";
    public static final String JSON_FILE_DEFAILT_NAME = "configuration.json";
    public static final String YAML_FILE_DEFAULT_NAME = "configuration.yml";
    public static final String XML_FILE_DEFAULT_NAME = "configuration.xml";

    @NotNull
    public static AppConfig parseConfigFile() {
        try {
            Configuration config;
            Configurations configs = new Configurations();
            if (Files.exists(Path.of(PROPERTIES_FILE_DEFAULT_NAME))) {
                config = configs.properties(new File(PROPERTIES_FILE_DEFAULT_NAME));
            } else if (Files.exists(Path.of(JSON_FILE_DEFAILT_NAME))) {
                JSONConfiguration jsonConfiguration = new JSONConfiguration();
                jsonConfiguration.read(Files.newInputStream(Path.of(JSON_FILE_DEFAILT_NAME)));
                config = jsonConfiguration;
            } else if (Files.exists(Path.of(YAML_FILE_DEFAULT_NAME))) {
                YAMLConfiguration yamlConfiguration = new YAMLConfiguration();
                yamlConfiguration.read(Files.newInputStream(Path.of(YAML_FILE_DEFAULT_NAME)));
                config = yamlConfiguration;
            } else if (Files.exists(Path.of(XML_FILE_DEFAULT_NAME))) {
                config = configs.xml(XML_FILE_DEFAULT_NAME);
            } else {
                throw new IllegalArgumentException("Could not find a config file in the current directory");
            }
            return new AppConfig(config);
        } catch (ConfigurationException|IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static AppConfig parseConfigFile(String filePath) {
        try {
            Configuration config;
            Configurations configs = new Configurations();
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
            return new AppConfig(config);
        } catch (ConfigurationException|IOException e) {
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
