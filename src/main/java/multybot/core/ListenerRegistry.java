package multybot.core;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.hooks.EventListener;
import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@ApplicationScoped
public class ListenerRegistry {

    private static final Logger LOG = Logger.getLogger(ListenerRegistry.class);

    // Prevent double-registration in dev mode / restarts.
    private static final AtomicBoolean registered = new AtomicBoolean(false);

    @Inject
    Instance<EventListener> listeners;

    public void registerAll(JDA jda) {
        if (jda == null) {
            LOG.warn("JDA is null. Skipping listener registration.");
            return;
        }

        if (!registered.compareAndSet(false, true)) {
            LOG.info("Listeners already registered. Skipping.");
            return;
        }

        List<EventListener> list = new ArrayList<>();
        for (EventListener l : listeners) {
            list.add(l);
        }

        LOG.infof("Registering %d JDA listeners: %s",
                list.size(),
                list.stream()
                        .map(x -> x.getClass().getSimpleName())
                        .sorted()
                        .toList()
        );

        // Defensive: remove first (in case something registered them elsewhere).
        jda.removeEventListener(list.toArray(new Object[0]));
        jda.addEventListener(list.toArray(new Object[0]));
    }
}