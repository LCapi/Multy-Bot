package multybot.core;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jboss.logging.Logger;

import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class CommandRouter {
    private static final Logger LOG = Logger.getLogger(CommandRouter.class);

    @Inject
    List<Command> commands = Collections.emptyList();

    public List<Command> commands() {
        return commands;
    }

    public Optional<Command> find(String name) {
        if (name == null) return Optional.empty();
        for (var c : commands) {
            if (name.equalsIgnoreCase(c.name())) return Optional.of(c);
        }
        return Optional.empty();
    }

    public List<SlashCommandData> slashData(Locale locale) {
        return commands.stream()
                .map(c -> c.slashData(locale))
                .collect(Collectors.toList());
    }

    /** Registro en una GUILD concreta (dev) */
    public void registerGuild(JDA jda, String guildId, Locale locale) {
        var guild = jda.getGuildById(guildId);
        if (guild == null) {
            LOG.warnf("Guild no encontrada: %s", guildId);
            return;
        }
        var data = slashData(locale);
        guild.updateCommands().addCommands(data).queue(v ->
                LOG.infof("Registrados %d comandos en guild %s (%s).", v.size(), guild.getName(), guild.getId())
        );
    }

    /** Registro GLOBAL */
    public void registerGlobal(JDA jda, Locale locale) {
        var data = slashData(locale);
        jda.updateCommands().addCommands(data).queue(v ->
                LOG.infof("Registrados %d comandos GLOBAL.", v.size())
        );
    }

    /** Ejecuta el comando correspondiente a la interacción ya deferida en CommandContext */
    public void route(CommandContext ctx) {
        var name = ctx.event().getName();
        find(name).ifPresentOrElse(
                c -> {
                    try {
                        c.execute(ctx);
                    } catch (Exception e) {
                        LOG.errorf(e, "Fallo ejecutando /%s", name);
                        ctx.hook().editOriginal("Ocurrió un error ejecutando el comando.").queue();
                    }
                },
                () -> ctx.hook().editOriginal("Comando no reconocido.").queue()
        );
    }
}
