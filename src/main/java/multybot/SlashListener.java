package multybot;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import multybot.core.Command;
import multybot.core.CommandContext;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import org.jboss.logging.Logger;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Resuelve y ejecuta comandos de barra.
 */
@ApplicationScoped
public class SlashListener extends ListenerAdapter {

    private static final Logger LOG = Logger.getLogger(SlashListener.class);

    private final Map<String, Command> byName = new HashMap<>();

    @Inject
    public SlashListener(Instance<Command> commandsProvider) {
        // Indexar por nombre para resolución O(1)
        for (Command c : commandsProvider) {
            byName.put(c.name(), c);
        }
        LOG.infof("SlashListener inicializado. Comandos registrados: %s", byName.keySet());
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        LOG.infof("Slash: /%s by %s in %s", event.getName(), event.getUser().getAsTag(),
                event.getChannel().getName());

        // Si el comando es rápido (ping, help simple), responde directo:
        if (isInstantCommand(event.getName())) {
            handleInstant(event); // dentro hará event.reply(...).queue()
            return;
        }

        // Para el resto, ACK inmediato y luego trabajas
        event.deferReply().queue(); // o setEphemeral(true) si procede
        try {
            route(event); // tu lógica: ejecuta el comando y luego usa event.getHook() para responder
        } catch (Exception e) {
            LOG.error("Error ejecutando comando: " + event.getName(), e);
            event.getHook().editOriginal("Ha ocurrido un error inesperado.").queue();
        }
    }

}
