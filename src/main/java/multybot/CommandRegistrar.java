package multybot;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import multybot.core.CommandRouter;

import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import org.jboss.logging.Logger;

import java.util.Locale;

@ApplicationScoped
public class CommandRegistrar extends ListenerAdapter {

    private static final Logger LOG = Logger.getLogger(CommandRegistrar.class);

    @Inject CommandRouter router;

    @Override
    public void onReady(ReadyEvent event) {
        LOG.info("JDA Ready → registrando slash commands…");
        // Si quieres que respete el idioma del sistema, usa Locale.getDefault()
        router.discoverAndRegister(event.getJDA(), Locale.ENGLISH);
    }
}
