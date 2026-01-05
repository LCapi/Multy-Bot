package multybot.core;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class FeatureGate {

    // If empty -> enable everything (dev-friendly)
    private final Set<String> enabledCommands;
    private final Set<String> enabledComponents;

    public FeatureGate(
            @ConfigProperty(name = "multybot.features.enabled-commands")
            Optional<String> enabledCommandsCsv,
            @ConfigProperty(name = "multybot.features.enabled-components")
            Optional<String> enabledComponentsCsv
    ) {
        this.enabledCommands = parseCsv(enabledCommandsCsv.orElse(""));
        this.enabledComponents = parseCsv(enabledComponentsCsv.orElse(""));
    }

    public boolean isCommandEnabled(String commandName) {
        if (enabledCommands.isEmpty()) return true;
        if (commandName == null) return false;
        return enabledCommands.contains(commandName.trim().toLowerCase());
    }

    public boolean isComponentEnabled(String componentKey) {
        if (enabledComponents.isEmpty()) return true;
        if (componentKey == null) return false;
        return enabledComponents.contains(componentKey.trim().toLowerCase());
    }

    private static Set<String> parseCsv(String csv) {
        if (csv == null) return Collections.emptySet();
        String trimmed = csv.trim();
        if (trimmed.isEmpty()) return Collections.emptySet();

        return Arrays.stream(trimmed.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toLowerCase)
                .collect(Collectors.toCollection(HashSet::new));
    }
}