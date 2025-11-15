package multybot.core;

import jakarta.enterprise.context.ApplicationScoped;

import org.jboss.logging.Logger;

import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

/**
 * Listener para engancharse al Ready y registrar comandos.
 * En este punto solo mostramos logs. El registro real de slash commands
 * puedes mantenerlo aquí o delegarlo a tu CommandRouter (si ya lo tienes).
 */
@ApplicationScoped
public class CommandRegistrar extends ListenerAdapter {

    private static final Logger LOG = Logger.getLogger(CommandRegistrar.class);

    @Override
    public void onReady(ReadyEvent event) {
        LOG.infof("JDA Ready → registrando slash commands… Guilds: %d",
                event.getJDA().getGuilds().size());

        // Si tienes un CommandRouter, llámalo aquí:
        // commandRouter.discoverAndRegister(event.getJDA());
        // De momento mantenemos solo el log para validar el flujo.
    }
}