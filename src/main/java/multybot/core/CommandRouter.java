package multybot.core;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jboss.logging.Logger;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@ApplicationScoped
public class CommandRouter {
    private static final Logger LOG = Logger.getLogger(CommandRouter.class);

    /** Quarkus inyecta todos los beans que implementen Command (@ApplicationScoped). */
    @Inject
    List<Command> commands;

    public List<Command> commands() { return commands; }

    public Optional<Command> find(String name) {
        if (name == null) return Optional.empty();
        return commands.stream().filter(c -> name.equalsIgnoreCase(c.name())).findFirst();
    }

    public List<SlashCommandData> slashData(Locale locale) {
        return commands.stream()
                .sorted(Comparator.comparing(Command::name))
                .map(c -> c.slashData(locale))
                .toList();
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

    /** Ejecuta el comando correspondiente a la interacción (ya deferida si hace falta). */
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