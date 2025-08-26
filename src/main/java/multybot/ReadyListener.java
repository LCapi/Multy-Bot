package multybot;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import multybot.core.CommandRouter;

import java.util.Locale;

@ApplicationScoped
public class ReadyListener extends ListenerAdapter {
    @Inject CommandRouter router;
    @Inject DiscordGateway gw;

    @Override
    public void onReady(ReadyEvent event) {
        // Usa 'es' como base de registro (las descripciones vienen de i18n)
        router.discoverAndRegister(gw.jda(), new Locale("es"));
    }
}
