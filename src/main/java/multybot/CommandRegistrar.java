package multybot;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import multybot.core.Command;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@ApplicationScoped
public class CommandRegistrar extends ListenerAdapter {

    private static final Logger LOG = Logger.getLogger(CommandRegistrar.class);

    @Inject Instance<Command> commands;
    @Inject DiscordGateway gateway;

    // Si configuras este ID, registramos como GUILD commands (instantáneo)
    @ConfigProperty(name = "bot.devGuildId", defaultValue = "")
    String devGuildId;

    @Override
    public void onReady(ReadyEvent event) {
        try {
            JDA jda = gateway.jda();
            if (jda == null) {
                LOG.warn("No JDA instance available en onReady. Cancelando registro de comandos.");
                return;
            }

            // Construir la lista de CommandData con un locale por defecto
            List<CommandData> data = new ArrayList<>();
            for (Command c : commands) {
                data.add(c.slashData(Locale.ENGLISH));
            }

            if (data.isEmpty()) {
                LOG.warn("No commands discovered to register.");
                return;
            }

            if (!devGuildId.isBlank()) {
                Guild g = jda.getGuildById(devGuildId);
                if (g == null) {
                    LOG.warnf("bot.devGuildId=%s no corresponde a un guild visible por el bot. Registrando global.", devGuildId);
                    jda.updateCommands().addCommands(data).queue(
                            s -> LOG.infof("Registered %d GLOBAL commands (fallback).", data.size()),
                            e -> LOG.error("Failed to register GLOBAL commands", e)
                    );
                    return;
                }
                // Registro como comandos de GUILD (aparecen al instante)
                g.updateCommands().addCommands(data).queue(
                        s -> LOG.infof("Registered %d GUILD commands in %s (%s).", data.size(), g.getName(), g.getId()),
                        e -> LOG.error("Failed to register GUILD commands", e)
                );
            } else {
                // Registro GLOBAL (propagación lenta)
                jda.updateCommands().addCommands(data).queue(
                        s -> LOG.infof("Registered %d GLOBAL commands.", data.size()),
                        e -> LOG.error("Failed to register GLOBAL commands", e)
                );
            }
        } catch (Exception ex) {
            LOG.error("Error registering slash commands on ReadyEvent", ex);
        }
    }
}
