package multybot;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import multybot.core.CommandContext;
import multybot.core.CommandRouter;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jboss.logging.Logger;

@ApplicationScoped
public class SlashListener extends ListenerAdapter {
    private static final Logger LOG = Logger.getLogger(SlashListener.class);

    @Inject
    CommandRouter router;

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        var ctx = CommandContext.fromEvent(event); // hace deferReply
        router.route(ctx);
    }
}
