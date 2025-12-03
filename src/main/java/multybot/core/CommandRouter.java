package multybot.core;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.enterprise.inject.Instance;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jboss.logging.Logger;

import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class CommandRouter {

    private static final Logger LOG = Logger.getLogger(CommandRouter.class);

    @Inject
    Instance<Command> commandInstances;   // instead of List<Command>

    private Map<String, Command> commandsByName = Collections.emptyMap();

    @PostConstruct
    void init() {
        // materialize list from Instance<Command>
        List<Command> commands = commandInstances.stream().toList();

        this.commandsByName = commands.stream()
                .collect(Collectors.toMap(
                        Command::name,
                        c -> c,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        LOG.infof("Loaded %d commands: %s",
                commandsByName.size(),
                String.join(", ", commandsByName.keySet()));
    }

    /**
     * Central dispatch entry point for slash commands.
     */
    public void dispatch(SlashCommandInteractionEvent event) {
        String name = event.getName();
        Guild guild = event.getGuild();
        String guildId = guild != null ? guild.getId() : "DM";

        LOG.infof("Dispatching slash command '%s' (guild=%s, user=%s)",
                name,
                guildId,
                event.getUser().getAsTag());

        CommandContext ctx = CommandContext.from(event);

        Command cmd = commandsByName.get(name);
        if (cmd == null) {
            // Unknown command: reply ephemeral and exit
            ctx.replyEphemeral("Unknown command: `" + name + "`");
            return;
        }

        try {
            cmd.execute(ctx);
        } catch (Exception e) {
            LOG.errorf(e, "Error executing command '%s'", name);
            ctx.replyEphemeral("An internal error occurred while executing this command.");
        }
    }

    /**
     * Used by HelpCommand (and others) to see all registered commands.
     */
    public Collection<Command> allCommands() {
        return commandsByName.values();
    }

    /**
     * Register commands globally.
     * (Used by ReadyListener / CommandRegistrar or similar).
     */
    public void registerGlobal(JDA jda, Locale locale) {
        LOG.info("Registering global application commands...");
        jda.updateCommands()
                .addCommands(
                        commandsByName.values().stream()
                                .map(c -> c.slashData(locale))
                                .toList()
                )
                .queue(
                        s -> LOG.info("Global commands registered."),
                        e -> LOG.error("Failed to register global commands", e)
                );
    }

    /**
     * Register commands for a specific guild (dev / staging).
     */
    public void registerGuild(JDA jda, String guildId, Locale locale) {
        Guild guild = jda.getGuildById(guildId);
        if (guild == null) {
            LOG.warnf("Could not find dev guild id=%s to register commands", guildId);
            return;
        }

        LOG.infof("Registering guild commands for guild=%s", guildId);
        guild.updateCommands()
                .addCommands(
                        commandsByName.values().stream()
                                .map(c -> c.slashData(locale))
                                .toList()
                )
                .queue(
                        s -> LOG.infof("Commands registered in guild %s", guildId),
                        e -> LOG.errorf(e, "Failed to register commands in guild %s", guildId)
                );
    }
}