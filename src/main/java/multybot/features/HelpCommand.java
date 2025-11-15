package multybot.features;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import multybot.core.AbstractCommand;
import multybot.core.Command;
import multybot.core.CommandContext;
import multybot.core.CommandRouter;

import java.util.Comparator;
import java.util.stream.Collectors;

@ApplicationScoped
public class HelpCommand extends AbstractCommand {

    @Inject
    CommandRouter router;

    @Override public String name() { return "help"; }

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