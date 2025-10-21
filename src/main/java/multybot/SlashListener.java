package multybot;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import multybot.core.Command;
import multybot.core.CommandContext;

import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;

import org.jboss.logging.Logger;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@ApplicationScoped
public class SlashListener extends ListenerAdapter {

    private static final Logger LOG = Logger.getLogger(SlashListener.class);

    @Inject Instance<Command> commandBeans; // todos los @DiscordCommand
    private final Map<String, Command> commands = new HashMap<>();

    @PostConstruct
    void init() {
        for (Command c : commandBeans) {
            commands.put(c.name(), c);
        }
        LOG.infof("SlashListener inicializado. Comandos registrados: %s", commands.keySet());
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        try {
            final String name = event.getName();
            final Command cmd = commands.get(name);
            if (cmd == null) {
                event.reply("Unknown command: " + name).setEphemeral(true).queue();
                return;
            }

            // JDA 5: NO existe queueAndWait(). Obtener el hook de forma bloqueante:
            InteractionHook hook = event.deferReply().submit().join();

            // Locale por defecto; cuando tengas GuildConfig, cámbialo
            Locale locale = Locale.ENGLISH;

            CommandContext ctx = new CommandContext(
                    event,
                    event.getJDA(),
                    event.getGuild(),
                    event.getMember(),
                    locale,
                    hook
            );

            cmd.execute(ctx);

        } catch (Exception e) {
            LOG.error("Error manejando slash interaction", e);
            if (event.isAcknowledged()) {
                event.getHook().sendMessage("Something went wrong while executing the command.")
                        .setEphemeral(true).queue();
            } else {
                event.reply("Something went wrong while executing the command.")
                        .setEphemeral(true).queue();
            }
        }
    }
}
