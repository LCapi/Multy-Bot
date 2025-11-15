package multybot.core;

import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import java.util.Locale;

public interface Command {
    String name();                                // id del slash (/ping)
    default String description(Locale locale) {
        return "No description configured.";
    }// i18n
    SlashCommandData slashData(Locale locale);    // definición del slash
    void execute(CommandContext ctx);             // lógica
}