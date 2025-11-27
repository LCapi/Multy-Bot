package multybot.core;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jboss.logging.Logger;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class CommandRouter {

    private final Logger log;
    private final Map<String, Command> commandsByName = new HashMap<>();

    @Inject
    public CommandRouter(Instance<Command> commands, Logger log) {
        this.log = log;

        for (Command cmd : commands) {
            if (cmd == null) {
                continue;
            }
            String name = cmd.name();
            if (name == null || name.isBlank()) {
                log.warnf("Skipping Command with blank name: %s", cmd.getClass().getName());
                continue;
            }

            Command previous = commandsByName.put(name, cmd);
            if (previous != null) {
                log.warnf("Duplicate Command name '%s' between %s and %s",
                        name,
                        previous.getClass().getName(),
                        cmd.getClass().getName());
            }
        }

        log.infof("Registered %d commands: %s", commandsByName.size(), commandsByName.keySet());
    }

    /**
     * Main entry for slash command execution.
     */
    public void dispatch(SlashCommandInteractionEvent event) {
        String name = event.getName();
        String guildId = event.getGuild() != null ? event.getGuild().getId() : "DM";
        String userTag = event.getUser() != null ? event.getUser().getAsTag() : "unknown";

        log.infof("Dispatching slash command '%s' (guild=%s, user=%s)", name, guildId, userTag);

        Command cmd = commandsByName.get(name);
        CommandContext ctx = CommandContext.from(event);

        if (cmd == null) {
            // Unknown command: respond gracefully
            ctx.replyEphemeral("Unknown command: " + name);
            return;
        }

        try {
            cmd.execute(ctx);
        } catch (Exception e) {
            log.errorf(e, "Error executing command '%s' for user %s in guild %s", name, userTag, guildId);
            try {
                ctx.replyEphemeral("There was an error while executing this command.");
            } catch (Exception ignored) {
                // At least we logged it
            }
        }
    }

    /**
     * Expose all commands for HelpCommand or other features.
     */
    public Collection<Command> allCommands() {
        return Collections.unmodifiableCollection(commandsByName.values());
    }
}