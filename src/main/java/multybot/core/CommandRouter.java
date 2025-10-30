package multybot.core;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@ApplicationScoped
public class CommandRouter {
    private static final Logger LOG = Logger.getLogger(CommandRouter.class);

    @Inject JDA jda;

    // Quarkus inyecta todas las implementaciones de Command
    @Inject List<Command> commands = new ArrayList<>();

    public Command find(String name) {
        for (var c : commands) {
            if (c.slashData(Locale.ENGLISH).getName().equalsIgnoreCase(name)) {
                return c;
            }
        }
        return null;
    }

    public List<SlashCommandData> slashData(Locale locale) {
        return commands.stream().map(c -> c.slashData(locale)).toList();
    }

    public List<Command> all() { return commands; }
}
