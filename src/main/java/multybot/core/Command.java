package multybot.core;

import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import java.util.Locale;

public interface Command {

    // Definición del slash command (nombre, descripción, opciones…)
    SlashCommandData slashData(Locale locale);

    // Ejecución del comando
    void execute(CommandContext ctx) throws Exception;

    // Por defecto, los comandos son rápidos. Sobrescribe en comandos lentos (I/O, APIs, DB…)
    default boolean isLongRunning() {
        return false;
    }
}
