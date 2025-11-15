package multybot.core;

import jakarta.enterprise.context.ApplicationScoped;

import org.jboss.logging.Logger;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

/**
 * Listener central para slash commands.
 * Por ahora solo hace log y responde algo simple para validar el ciclo.
 * Luego podrás delegar en tu CommandRouter/CommandContext.
 */
@ApplicationScoped
public class InteractionListener extends ListenerAdapter {

    private static final Logger LOG = Logger.getLogger(InteractionListener.class);

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String name = event.getName();
        LOG.infof("SlashCommand recibido: /%s en guild %s por %s",
                name,
                event.getGuild() != null ? event.getGuild().getId() : "DM",
                event.getUser().getAsTag());

        // Respuesta mínima para evitar timeouts:
        // Discord exige ACK en <= 3s
        try {
            event.reply("OK: " + name).setEphemeral(true).queue();
        } catch (Exception e) {
            LOG.error("Fallo respondiendo interacción", e);
        }
    }
}