package multybot.core;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

@ApplicationScoped
public class InteractionListener extends ListenerAdapter {

    private static final Logger LOG = Logger.getLogger(InteractionListener.class);

    @Inject
    CommandRouter commandRouter;

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String name = event.getName();

        LOG.infof(
                "SlashCommand received: /%s in guild %s by %s",
                name,
                event.getGuild() != null ? event.getGuild().getId() : "DM",
                event.getUser().getAsTag()
        );

        // Optional: for now we only support guild commands
        if (event.getGuild() == null) {
            event.reply("This bot only works in servers for now.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Delegate everything to CommandRouter
        try {
            commandRouter.dispatch(event);
        } catch (Exception e) {
            LOG.errorf(e, "Error while dispatching slash command '/%s'", name);

            // Safety net in case router did not manage to reply
            if (!event.isAcknowledged()) {
                event.reply("An internal error occurred while processing this command.")
                        .setEphemeral(true)
                        .queue();
            } else {
                event.getHook().sendMessage("An internal error occurred while processing this command.")
                        .setEphemeral(true)
                        .queue();
            }
        }
    }
}