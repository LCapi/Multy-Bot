package multybot;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

@ApplicationScoped
public class DiscordGateway {
    private static final Logger LOG = Logger.getLogger(DiscordGateway.class);

    @ConfigProperty(name = "discord.token", defaultValue = "")
    String token;

    @ConfigProperty(name = "bot.gateway.enabled", defaultValue = "true")
    boolean enabled;

    @Inject CommandRegistrar registrar;
    @Inject InteractionListener interactions;

    private JDA jda;
    public JDA jda() { return jda; }

    void onStart(@Observes StartupEvent ev) throws Exception {
        if (!enabled || token.isBlank()) {
            LOG.warnf("DiscordGateway deshabilitado (%s). Arranca sin Discord.",
                    enabled ? "faltó DISCORD_TOKEN" : "bot.gateway.enabled=false");
            return;
        }

        jda = JDABuilder.createDefault(token)
                .addEventListeners(registrar, interactions)
                .build();

        LOG.info("Conectando a Discord...");
        jda.awaitReady();
        LOG.info("JDA listo");
    }
}
