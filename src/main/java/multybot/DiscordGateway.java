package dev.lcapi.multybot;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.runtime.ShutdownEvent;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import dev.lcapi.multybot.core.CommandRouter;
import dev.lcapi.multybot.infra.I18n;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

import java.util.Locale;

@ApplicationScoped
public class DiscordGateway {
    @ConfigProperty(name="discord.token") String token;

    @Inject ReadyListener ready;
    @Inject InteractionListener interactions;

    private JDA jda;

    void onStart(@Observes StartupEvent ev) throws Exception {
        jda = JDABuilder.createDefault(token)
                .addEventListeners(ready, interactions)
                .build();
        jda.awaitReady();
    }

    void onStop(@Observes ShutdownEvent ev) {
        if (jda != null) jda.shutdown();
    }

    public JDA jda() { return jda; }
}
