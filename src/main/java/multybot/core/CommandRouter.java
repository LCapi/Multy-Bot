package multybot.core;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class CommandRouter {

    private static final Logger LOG = Logger.getLogger(CommandRouter.class);

    @Inject @Any
    Instance<Command> commandBeans;

    @ConfigProperty(name = "bot.dev.guild-id", defaultValue = "")
    String devGuildId;

    private final Map<String, Command> byName = new HashMap<>();

    @PostConstruct
    void init() {
        for (Command c : commandBeans) {
            byName.put(c.name(), c);
        }
        LOG.infof("CommandRouter: cargados %d comandos -> %s", byName.size(), byName.keySet());
    }

    /** Lista inmutable de comandos para cosas como /help. */
    public Collection<Command> commands() {
        return Collections.unmodifiableCollection(byName.values());
    }

    /** Enruta al comando correspondiente. */
    public void route(CommandContext ctx) {
        String name = ctx.event().getName();
        Command cmd = byName.get(name);
        if (cmd == null) {
            ctx.event().reply("Unknown command: `" + name + "`").setEphemeral(true).queue();
            return;
        }
        try {
            cmd.execute(ctx);
        } catch (Exception e) {
            LOG.errorf(e, "Error ejecutando comando %s", name);
            ctx.event().reply("Internal error running this command.").setEphemeral(true).queue();
        }
    }

    /** Registro de comandos aceptando Locale explícito (lo que te pedían CommandRegistrar/ReadyListener). */
    public void discoverAndRegister(JDA jda, java.util.Locale locale) {
        // Cada Command entrega su propio SlashCommandData vía slashData(locale)
        List<CommandData> data = byName.values().stream()
                .map(c -> c.slashData(locale))
                .collect(Collectors.toList());

        if (devGuildId != null && !devGuildId.isBlank()) {
            Guild g = jda.getGuildById(devGuildId);
            if (g == null) {
                LOG.warnf("No se encontró el GUILD con id=%s. Registraré GLOBAL.", devGuildId);
                jda.updateCommands().addCommands(data).queue(
                        _ok -> LOG.infof("Registrados %d comandos GLOBAL.", data.size()),
                        err -> LOG.error("Fallo registrando comandos GLOBAL", err)
                );
            } else {
                g.updateCommands().addCommands(data).queue(
                        _ok -> LOG.infof("Registrados %d comandos en guild %s (%s).",
                                data.size(), g.getName(), g.getId()),
                        err -> LOG.errorf(err, "Fallo registrando comandos en guild %s", g.getId())
                );
            }
        } else {
            jda.updateCommands().addCommands(data).queue(
                    _ok -> LOG.infof("Registrados %d comandos GLOBAL.", data.size()),
                    err -> LOG.error("Fallo registrando comandos GLOBAL", err)
            );
        }
    }

    /** Sobrecarga por comodidad (usa EN por defecto). */
    public void discoverAndRegister(JDA jda) {
        discoverAndRegister(jda, java.util.Locale.ENGLISH);
    }
}
