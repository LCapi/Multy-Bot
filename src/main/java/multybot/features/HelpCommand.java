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

    @Override
    public String name() {
        return "help";
    }

    @Override
    public String description(Locale locale) {
        return "Muestra la lista de comandos";
    }

    @Override
    public SlashCommandData slashData(Locale locale) {
        return Commands.slash(name(), description(locale));
    }

    @Override
    public void execute(CommandContext ctx) {
        var list = router.commands().stream()
                .sorted(Comparator.comparing(Command::name))
                .map(c -> "/" + c.name() + " – " + c.description(ctx.locale()))
                .collect(Collectors.joining("\n"));

        if (list.isEmpty()) list = "No hay comandos registrados.";

        ctx.hook().editOriginal(list).queue();
    }
}
