package multybot;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import multybot.core.CommandRouter;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Locale;

@ApplicationScoped
public class ReadyListener extends ListenerAdapter {
    private static final Logger LOG = Logger.getLogger(ReadyListener.class);

    @Inject
    CommandRouter router;

    @ConfigProperty(name = "bot.commands.scope", defaultValue = "GUILD")
    String scope;

    @ConfigProperty(name = "bot.dev.guild-id", defaultValue = "")
    String devGuildId;

    @Override
    public void onReady(ReadyEvent event) {
        LOG.info("JDA Ready → registrando slash commands…");
        var jda = event.getJDA();
        var locale = Locale.ENGLISH; // o carga desde config si quieres

        if ("GLOBAL".equalsIgnoreCase(scope)) {
            router.registerGlobal(jda, locale);
        } else {
            router.registerGuild(jda, devGuildId, locale);
        }
    }
}