package multybot;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import multybot.core.ListenerRegistry;
import net.dv8tion.jda.api.JDA;
import org.jboss.logging.Logger;

@ApplicationScoped
public class DiscordGateway {

    private static final Logger LOG = Logger.getLogger(DiscordGateway.class);

    @Inject JDA jda;
    @Inject ListenerRegistry listenerRegistry;

    void onStart(@Observes StartupEvent ev) {
        LOG.info("Starting DiscordGateway...");
        listenerRegistry.registerAll(jda);
        LOG.info("DiscordGateway started (listeners wired).");
    }

    void onStop(@Observes ShutdownEvent ev) {
        if (jda != null) {
            LOG.info("Shutting down JDA...");
            jda.shutdownNow();
        }
    }
}