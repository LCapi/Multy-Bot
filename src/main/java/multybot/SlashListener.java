package multybot;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import multybot.core.Command;
import multybot.core.CommandContext;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import org.jboss.logging.Logger;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Resuelve y ejecuta comandos de barra.
 */
@ApplicationScoped
public class SlashListener extends ListenerAdapter {

    private static final Logger LOG = Logger.getLogger(SlashListener.class);

    private final Map<String, Command> byName = new HashMap<>();

    @Inject
    public SlashListener(Instance<Command> commandsProvider) {
        // Indexar por nombre para resolución O(1)
        for (Command c : commandsProvider) {
            byName.put(c.name(), c);
        }
        LOG.infof("SlashListener inicializado. Comandos registrados: %s", byName.keySet());
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        final String name = event.getName();
        final Command cmd = byName.get(name);

        if (cmd == null) {
            event.reply("Unknown command: " + name).setEphemeral(true).queue();
            return;
        }

        // Locale por guild (impl tuya; si no hay, EN por defecto)
        Locale locale = Locale.ENGLISH;
        try {
            // Si tienes GuildConfig y un I18nService, resuélvelo aquí:
            // locale = i18nService.resolveLocale(event.getGuild());
        } catch (Exception ignored) {}

        // Defer para tener InteractionHook y no bloquear
        event.deferReply().queue(hook -> {
            CommandContext ctx = new CommandContext(
                    event,
                    event.getJDA(),
                    event.getGuild(),
                    event.getMember(),
                    locale,
                    hook
            );
            try {
                cmd.handle(ctx);
            } catch (Exception ex) {
                LOG.errorf(ex, "Error ejecutando comando /%s", name);
                hook.sendMessage("An error occurred while executing this command.").queue();
            }
        }, error -> {
            LOG.errorf(error, "Error haciendo deferReply en /%s", name);
        });
    }
}
