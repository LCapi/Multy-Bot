package multybot.core;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import multybot.infra.I18n;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Registers slash commands in Discord when JDA is ready.
 */
@ApplicationScoped
public class CommandRegistrar extends ListenerAdapter {

    private static final Logger LOG = Logger.getLogger(CommandRegistrar.class);

    @Inject JDA jda;

    @Inject I18n i18n;

    @Inject jakarta.enterprise.inject.Instance<Command> commandBeans;

    /**
     * Call this once when JDA signals Ready.
     */
    public void registerSlashCommands() {
        List<SlashCommandData> slashCommands = new ArrayList<>();

        for (Command cmd : commandBeans) {
            DiscordCommand meta = cmd.getClass().getAnnotation(DiscordCommand.class);
            if (meta == null) {
                continue; // not a slash command
            }

            if (!meta.enabled()) {
                LOG.infof("Command '%s' is disabled via @DiscordCommand(enabled = false). Skipping registration.",
                        meta.name());
                continue;
            }

            try {
                SlashCommandData data = cmd.slashData(Locale.ENGLISH);
                if (data == null) {
                    LOG.warnf("Command '%s' returned null SlashCommandData. Skipping.", meta.name());
                    continue;
                }
                slashCommands.add(data);
            } catch (Exception e) {
                LOG.errorf(e, "Error building SlashCommandData for command '%s'", meta.name());
            }
        }

        if (slashCommands.isEmpty()) {
            LOG.warn("No slash commands to register (all disabled or failed to build).");
            return;
        }

        LOG.infof("Registering %d slash commands globally…", slashCommands.size());

        jda.updateCommands()
                .addCommands(slashCommands)
                .queue(
                        success -> LOG.info("Slash commands registered successfully."),
                        error -> LOG.error("Failed to register slash commands", error)
                );
    }
}