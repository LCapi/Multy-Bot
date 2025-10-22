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
    String token; // mapeado desde DISCORD_TOKEN

    @ConfigProperty(name = "bot.gateway.enabled", defaultValue = "true")
    boolean enabled;

    @Inject SlashListener slashListener;
    @Inject CommandRegistrar commandRegistrar; // ⬅ también lo añadimos como listener

    private JDA jda;
    public JDA jda() { return jda; }

    void onStart(@Observes StartupEvent ev) throws Exception {
        if (!enabled || token.isBlank()) {
            LOG.warnf("DiscordGateway deshabilitado (%s). Arrancando sin conectar a Discord.",
                    enabled ? "faltó DISCORD_TOKEN" : "bot.gateway.enabled=false");
            return;
        }

        jda = JDABuilder.createDefault(token)
                .addEventListeners(
                        slashListener,
                        commandRegistrar // ⬅ se registrará al recibir ReadyEvent
                )
                .build();

        LOG.info("Conectando a Discord...");
        jda.awaitReady();
        LOG.info("JDA listo");
    }
}
