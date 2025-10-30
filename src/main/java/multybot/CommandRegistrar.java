package multybot;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import multybot.core.CommandRouter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.util.Locale;

@ApplicationScoped
public class CommandRegistrar extends ListenerAdapter {
    private static final Logger LOG = Logger.getLogger(CommandRegistrar.class);

    @Inject JDA jda;
    @Inject CommandRouter router;

    @ConfigProperty(name = "bot.dev.guild-id", defaultValue = "")
    String devGuildId;

    @Override
    public void onReady(ReadyEvent event) {
        var data = router.slashData(Locale.ENGLISH);
        if (devGuildId != null && !devGuildId.isBlank()) {
            var guild = jda.getGuildById(devGuildId);
            if (guild != null) {
                guild.updateCommands().addCommands(data).queue(v ->
                        LOG.infof("Registrados %d comandos en guild %s (%s).",
                                v.size(), guild.getName(), guild.getId()));
            } else {
                LOG.warnf("Guild dev no encontrada: %s", devGuildId);
            }
        } else {
            jda.updateCommands().addCommands(data).queue(v ->
                    LOG.infof("Registrados %d comandos GLOBAL (pueden tardar en verse).", v.size()));
        }
    }
}
