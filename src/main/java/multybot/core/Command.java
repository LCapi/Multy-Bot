package multybot.core;

import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.Locale;

public interface Command {

    /** Definición del slash command (localizable) */
    SlashCommandData slashData(Locale locale);

    /** Lógica principal del comando */
    void execute(CommandContext ctx) throws Exception;

    /** Descripción localizable (default para no romper comandos existentes) */
    default String description(Locale locale) {
        return "No description";
    }

    /** Nombre del comando: si no lo das, se infiere de slashData(...) */
    default String name(Locale locale) {
        try {
            return slashData(locale).getName();
        } catch (Exception ignored) {
            return "command";
        }
    }

    /** Para comandos que tardan, nos permite deferir automáticamente */
    default boolean isLongRunning() {
        return false;
    }
}