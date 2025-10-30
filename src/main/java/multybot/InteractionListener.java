package multybot;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import multybot.core.CommandContext;
import multybot.core.CommandRouter;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jboss.logging.Logger;

@ApplicationScoped
public class InteractionListener extends ListenerAdapter {
    private static final Logger LOG = Logger.getLogger(InteractionListener.class);

    @Inject CommandRouter router;

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        try {
            var cmd = router.find(event.getName());
            if (cmd == null) {
                event.reply("Unknown command.").setEphemeral(true).queue();
                return;
            }
            var ctx = CommandContext.from(event);

            // Si es potencialmente lento, ACK inmediato diferido
            if (cmd.isLongRunning()) {
                ctx.defer(true); // puedes poner false si quieres visible
            }

            cmd.execute(ctx);

        } catch (Exception e) {
            LOG.error("Error executing command " + event.getName(), e);
            if (event.isAcknowledged()) {
                event.getHook().editOriginal("Unexpected error while executing the command.").queue();
            } else {
                event.reply("Unexpected error while executing the command.").setEphemeral(true).queue();
            }
        }
    }
}
