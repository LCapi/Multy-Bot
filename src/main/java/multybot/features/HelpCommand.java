package multybot.features;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import multybot.core.Command;
import multybot.core.CommandContext;
import multybot.core.CommandRouter;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.Comparator;
import java.util.Locale;
import java.util.stream.Collectors;

@ApplicationScoped
public class HelpCommand implements Command {

    @Inject
    CommandRouter router;

    // Firma nueva del contrato
    @Override
    public String name(Locale locale) {
        return "help";
    }

    @Override
    public String description(Locale locale) {
        return switch (locale.getLanguage()) {
            case "es" -> "Muestra la lista de comandos";
            default -> "Show the list of commands";
        };
    }

    @Override
    public SlashCommandData slashData(Locale locale) {
        return Commands.slash(name(locale), description(locale));
    }

    @Override
    public void execute(CommandContext ctx) {
        Locale locale = ctx.locale();

        String list = router.commands().stream()
                // Comparator usando el locale
                .sorted(Comparator.comparing(c -> c.name(locale)))
                .map(c -> "/" + c.name(locale) + " – " + c.description(locale))
                .collect(Collectors.joining("\n"));

        if (list.isEmpty()) {
            list = (locale.getLanguage().equals("es"))
                    ? "No hay comandos registrados."
                    : "No commands registered.";
        }

        // Respuesta segura (si no está acknowledged, reply; si lo está, hook.sendMessage)
        ctx.reply(list);
    }
}