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

    // Listeners opcionales (regístralos aquí si no lo haces en otro sitio)
    @Inject CommandRegistrar commandRegistrar;
    @Inject InteractionListener interactionListener;
    @Inject ReadyListener readyListener; // si lo usas

    private JDA jda;
    public JDA jda() { return jda; }

    void onStart(@Observes StartupEvent ev) throws Exception {
        if (!enabled || token.isBlank()) {
            LOG.warnf("DiscordGateway deshabilitado (%s). Arrancando sin conectar a Discord.",
                    enabled ? "faltó DISCORD_TOKEN" : "bot.gateway.enabled=false");
            return;
        }

        LOG.info("Conectando a Discord...");
        jda = JDABuilder.createDefault(token)
                .addEventListeners(
                        commandRegistrar,   // emite onReady → registra slash
                        interactionListener,
                        readyListener       // si tienes uno
                )
                .build();

        LOG.info("JDA listo");
    }
}