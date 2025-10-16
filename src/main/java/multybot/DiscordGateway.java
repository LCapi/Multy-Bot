package multybot;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

@ApplicationScoped
public class DiscordGateway {

    private static final Logger LOG = Logger.getLogger(DiscordGateway.class);

    @ConfigProperty(name = "discord.token", defaultValue = "")
    String token; // se mapea desde la env DISCORD_TOKEN

    @ConfigProperty(name = "bot.gateway.enabled", defaultValue = "true")
    boolean enabled;

    private JDA jda;
    public JDA jda() { return jda; }

    void onStart(@Observes StartupEvent ev) throws Exception {
        if (!enabled || token.isBlank()) {
            LOG.warnf("DiscordGateway deshabilitado (%s). Arrancando sin conectar a Discord.",
                    enabled ? "faltó DISCORD_TOKEN" : "bot.gateway.enabled=false");
            return; // ⬅️ no creamos JDA
        }

        jda = JDABuilder.createDefault(token)
                // .addEventListeners(ready, interactions, componentListener, ...)
                .build();
    }
}
