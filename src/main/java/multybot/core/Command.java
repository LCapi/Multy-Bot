package multybot.core;

import java.util.Locale;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public interface Command {

    // NUEVO contrato con locale
    String name(Locale locale);
    String description(Locale locale);
    SlashCommandData slashData(Locale locale);

    // COMPAT: antiguos llamadores que no pasan Locale
    default String name() { return name(Locale.ENGLISH); }
    default String description() { return description(Locale.ENGLISH); }
    default SlashCommandData slashData() { return slashData(Locale.ENGLISH); }

    default boolean isLongRunning() { return false; }

    void execute(CommandContext ctx) throws Exception;
}