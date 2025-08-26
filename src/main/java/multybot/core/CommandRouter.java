package multybot.core;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import multybot.infra.I18n;
import multybot.infra.PreconditionsExecutor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

@ApplicationScoped
public class CommandRouter {

    private final Map<String, Command> commands = new HashMap<>();

    @Inject Instance<Command> discovered;
    @Inject I18n i18n;
    @Inject PreconditionsExecutor preconditions;

    public void discoverAndRegister(JDA jda, Locale locale) {
        // Descubrir y registrar por CDI
        for (Command cmd : discovered) {
            DiscordCommand meta = cmd.getClass().getAnnotation(DiscordCommand.class);
            if (meta == null) continue;
            String name = meta.name();
            commands.put(name, cmd);
        }
        // Construir slash por idioma base (global)
        List<SlashCommandData> defs = commands.entrySet().stream().map(e -> {
            Command c = e.getValue();
            DiscordCommand meta = c.getClass().getAnnotation(DiscordCommand.class);
            String desc = i18n.msg(locale, meta.descriptionKey());
            SlashCommandData data = c.slashData(locale);
            data.setDescription(desc);
            return data;
        }).collect(Collectors.toList());
        jda.updateCommands().addCommands(defs).queue();
    }

    public void route(CommandContext ctx) throws Exception {
        String name = ctx.event().getName();
        Command cmd = commands.get(name);
        if (cmd == null) return;
        var failure = preconditions.evaluate(cmd, ctx);
        if (failure.isPresent()) {
            String msg = failure.get();
            ctx.event().reply(msg).setEphemeral(true).queue();
            return;
        }
        cmd.execute(ctx);
    }

    public String help(Locale locale) {
        return commands.keySet().stream().sorted()
                .map(n -> "• /" + n)
                .collect(Collectors.joining("\n"));
    }
}