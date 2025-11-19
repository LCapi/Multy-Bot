package multybot.core;

import jakarta.inject.Inject;
import multybot.infra.I18n;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import java.util.Locale;

public abstract class AbstractCommand implements Command {

    @Inject
    I18n i18n;

    @Override
    public String name() {
        DiscordCommand meta = getClass().getAnnotation(DiscordCommand.class);
        if (meta != null && !meta.name().isBlank()) {
            return meta.name();
        }

        // Fallback: class name without "Command", lowercased
        String simple = getClass().getSimpleName();
        if (simple.endsWith("Command")) {
            simple = simple.substring(0, simple.length() - "Command".length());
        }
        return simple.toLowerCase(Locale.ROOT);
    }

    @Override
    public String description(Locale locale) {
        DiscordCommand meta = getClass().getAnnotation(DiscordCommand.class);
        if (meta != null && !meta.descriptionKey().isBlank() && i18n != null) {
            return i18n.msg(locale, meta.descriptionKey());
        }
        return "No description available.";
    }

    @Override
    public SlashCommandData slashData(Locale locale) {
        return Commands.slash(name(), description(locale));
    }
}