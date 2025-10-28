package multybot.core;

import java.util.Locale;

import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

/**
 * Interfaz común para slash-commands.
 * Mantiene execute(..) por compatibilidad y añade handle(..) como punto único de entrada.
 */
public interface Command {

    /** nombre del comando (sin barra) */
    String name();

    /** definición del slash-command (por locale base) */
    SlashCommandData slashData(Locale locale);

    /** Implementado en tus clases de comando actuales (si ya existe, no toques esas clases). */
    void execute(CommandContext ctx) throws Exception;

    /** Punto de entrada unificado; por defecto delega a execute(..). */
    default void handle(CommandContext ctx) throws Exception {
        execute(ctx);
    }
}
