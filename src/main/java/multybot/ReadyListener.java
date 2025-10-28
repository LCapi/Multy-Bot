package multybot;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import multybot.core.CommandRouter;

import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Locale;

@ApplicationScoped
public class ReadyListener extends ListenerAdapter {

    @Inject CommandRouter router;

    @Override
    public void onReady(ReadyEvent event) {
        // compatibilidad con tu llamada antigua
        router.discoverAndRegister(event.getJDA(), Locale.getDefault());
    }
}
