package multybot.core;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class CommandRouter {

    private static final Logger LOG = Logger.getLogger(CommandRouter.class);

    @Inject Instance<Command> commandInstances;

    /**
     * If set, ONLY these commands will be enabled (whitelist mode).
     * Example: multybot.commands.enabled=ping,help,uptime
     */
    @ConfigProperty(name = "multybot.commands.enabled")
    Optional<List<String>> enabledList;

    /**
     * Commands to disable (applied after enabledList filter).
     * Example: multybot.commands.disabled=automod,poll
     */
    @ConfigProperty(name = "multybot.commands.disabled")
    Optional<List<String>> disabledList;

    private Map<String, Command> commandsByName = Collections.emptyMap();

    @PostConstruct
    void init() {
        List<Command> commands = commandInstances.stream().toList();

        this.commandsByName = commands.stream()
                .collect(Collectors.toMap(
                        c -> normalize(c.name()),
                        c -> c,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        LOG.infof("Loaded %d commands: %s",
                commandsByName.size(),
                String.join(", ", commandsByName.keySet()));

        LOGEnabledState();
    }

    /**
     * Central dispatch entry point for slash commands.
     */
    public void dispatch(SlashCommandInteractionEvent event) {
        String name = normalize(event.getName());
        Guild guild = event.getGuild();
        String guildId = guild != null ? guild.getId() : "DM";

        LOG.infof("Dispatching slash command '%s' (guild=%s, user=%s)",
                name, guildId, event.getUser().getAsTag());

        CommandContext ctx = CommandContext.from(event);

        // Feature gate (release flags)
        if (!isEnabled(name)) {
            ctx.replyEphemeral("This command is disabled in the current release.");
            return;
        }

        Command cmd = commandsByName.get(name);
        if (cmd == null) {
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
     * Used by HelpCommand (and others) to see commands.
     * If you want help to only show enabled commands, use enabledCommands().
     */
    public Collection<Command> allCommands() {
        return commandsByName.values();
    }

    /** Only the commands enabled by flags (recommended for help + registration). */
    public List<Command> enabledCommands() {
        Set<String> enabled = resolveEnabledSet();
        return commandsByName.entrySet().stream()
                .filter(e -> enabled.contains(e.getKey()))
                .map(Map.Entry::getValue)
                .toList();
    }

    /**
     * Register commands globally.
     */
    public void registerGlobal(JDA jda, Locale locale) {
        List<Command> cmds = enabledCommands();
        LOG.infof("Registering %d global application commands...", cmds.size());

        jda.updateCommands()
                .addCommands(cmds.stream().map(c -> c.slashData(locale)).toList())
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

        List<Command> cmds = enabledCommands();
        LOG.infof("Registering %d guild commands for guild=%s", cmds.size(), guildId);

        guild.updateCommands()
                .addCommands(cmds.stream().map(c -> c.slashData(locale)).toList())
                .queue(
                        s -> LOG.infof("Commands registered in guild %s", guildId),
                        e -> LOG.errorf(e, "Failed to register commands in guild %s", guildId)
                );
    }

    // ----------------- Flags logic -----------------

    private boolean isEnabled(String cmdName) {
        return resolveEnabledSet().contains(normalize(cmdName));
    }

    private Set<String> resolveEnabledSet() {
        // If enabledList is present -> whitelist mode
        Set<String> enabled;
        if (enabledList.isPresent() && !enabledList.get().isEmpty()) {
            enabled = enabledList.get().stream()
                    .map(this::normalize)
                    .collect(Collectors.toCollection(HashSet::new));
        } else {
            // default: everything discovered is enabled
            enabled = new HashSet<>(commandsByName.keySet());
        }

        // Apply blacklist
        if (disabledList.isPresent()) {
            for (String d : disabledList.get()) {
                enabled.remove(normalize(d));
            }
        }
        return enabled;
    }

    private void LOGEnabledState() {
        Set<String> enabled = resolveEnabledSet();
        LOG.infof("Enabled commands (%d): %s", enabled.size(), String.join(", ", enabled));
        if (enabledList.isPresent() && !enabledList.get().isEmpty()) {
            LOG.infof("Whitelist mode ON (multybot.commands.enabled=%s)", enabledList.get());
        }
        if (disabledList.isPresent() && !disabledList.get().isEmpty()) {
            LOG.infof("Blacklist applied (multybot.commands.disabled=%s)", disabledList.get());
        }
    }

    private String normalize(String s) {
        return s == null ? "" : s.trim().toLowerCase(Locale.ROOT);
    }
}