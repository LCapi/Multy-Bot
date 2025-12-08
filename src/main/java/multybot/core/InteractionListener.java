package multybot.core;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jboss.logging.Logger;

@ApplicationScoped
public class InteractionListener extends ListenerAdapter {

    private static final Logger LOG = Logger.getLogger(InteractionListener.class);

    @Inject
    CommandRouter commandRouter;

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        LOG.infof("SlashCommand received: /%s in guild %s by %s",
                event.getName(),
                event.getGuild() != null ? event.getGuild().getId() : "DM",
                event.getUser().getAsTag());

        // 1) ACK inmediato (siempre dentro de los 3s)
        event.deferReply().queue(
                success -> {
                    // 2) Ahora ya podemos hacer trabajo “lento” sin miedo
                    commandRouter.dispatch(event);
                },
                error -> LOG.error("Failed to defer reply for interaction " + event.getId(), error)
        );
    }
}