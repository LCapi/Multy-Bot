package multybot.core;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

/**
 * Central listener for slash commands.
 * It only logs and delegates to CommandRouter.
 */
@ApplicationScoped
public class InteractionListener extends ListenerAdapter {

    private static final Logger LOG = Logger.getLogger(InteractionListener.class);

    @Inject
    CommandRouter router;

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String name = event.getName();
        LOG.infof("SlashCommand received: /%s in guild %s by %s",
                name,
                event.getGuild() != null ? event.getGuild().getId() : "DM",
                event.getUser().getAsTag());

        try {
            // Delegate to the router: it will build CommandContext
            // and execute the corresponding Command
            router.dispatch(event);
        } catch (Exception e) {
            LOG.error("Failed to dispatch slash command", e);

            // Last-resort fallback: only if nothing has acknowledged the event
            try {
                if (!event.isAcknowledged()) {
                    event.reply("There was an internal error while handling this command.")
                            .setEphemeral(true)
                            .queue();
                }
            } catch (Exception ignored) {
                // At least we logged the error
            }
        }
    }
}