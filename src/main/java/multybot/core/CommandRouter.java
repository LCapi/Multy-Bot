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
    List<Command> commands; // <-- sin inicializar, CDI lo inyecta

    public List<Command> commands() {
        return commands;
    }

    /** Búsqueda teniendo en cuenta el Locale actual (los nombres pueden depender del idioma) */
    public Optional<Command> find(String name, Locale locale) {
        if (name == null) return Optional.empty();
        for (var c : commands) {
            if (name.equalsIgnoreCase(c.name(locale))) {
                return Optional.of(c);
            }
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
        var locale = ctx.locale();
        var name = ctx.event().getName();

        find(name, locale).ifPresentOrElse(
                c -> {
                    try {
                        c.execute(ctx);
                    } catch (Exception e) {
                        LOG.errorf(e, "Fallo ejecutando /%s", name);
                        ctx.reply(locale.getLanguage().equals("es")
                                ? "Ocurrió un error ejecutando el comando."
                                : "An error occurred while executing the command.");
                    }
                },
                () -> ctx.reply(locale.getLanguage().equals("es")
                        ? "Comando no reconocido."
                        : "Unknown command.")
        );
    }
}