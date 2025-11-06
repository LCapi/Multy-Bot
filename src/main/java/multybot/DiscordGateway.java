package multybot;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

import org.jboss.logging.Logger;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.Startup;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;

import multybot.core.CommandRegistrar;
import multybot.core.InteractionListener;

@ApplicationScoped
@Startup
public class DiscordGateway {

    private static final Logger LOG = Logger.getLogger(DiscordGateway.class);

    @ConfigProperty(name = "bot.token")
    String botToken;

    private JDA jda;

    // Listeners CDI-inyectados
    @Inject CommandRegistrar commandRegistrar;
    @Inject InteractionListener interactionListener;

    /**
     * Productor JDA para que esté disponible vía CDI en toda la app.
     * No registres listeners aquí para mantener una responsabilidad clara.
     */
    @Produces
    @ApplicationScoped
    public JDA produceJda() throws Exception {
        LOG.info("Construyendo JDA…");
        JDABuilder builder = JDABuilder.createDefault(botToken)
                .enableIntents(
                        GatewayIntent.GUILD_MEMBERS,
                        GatewayIntent.GUILD_MESSAGES,
                        GatewayIntent.MESSAGE_CONTENT,
                        GatewayIntent.GUILD_MODERATION
                )
                .setActivity(Activity.playing("Multy-Bot"));

        return builder.build(); // No bloquea; el READY llegará por evento
    }

    /**
     * Arranque del gateway: engancha listeners y espera a READY para dejar todo estable.
     */
    @PostConstruct
    void start() {
        try {
            // Obtiene la instancia producida por CDI
            this.jda = produceJda();

            // Registra listeners de la app (inyectados por CDI)
            jda.addEventListener(commandRegistrar, interactionListener);

            // Espera a que Discord marque READY (evita carreras en registro de comandos)
            jda.awaitReady();
            LOG.info("JDA Ready y listeners registrados.");
        } catch (Exception e) {
            LOG.error("Error arrancando DiscordGateway", e);
            throw new IllegalStateException("No se pudo iniciar JDA", e);
        }
    }

    @PreDestroy
    void stop() {
        if (jda != null) {
            LOG.info("Apagando JDA…");
            jda.shutdownNow();
        }
    }

    void onStop(@SuppressWarnings("unused") ShutdownEvent ev) {
        stop();
    }
}