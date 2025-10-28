package multybot.features;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import multybot.core.Command;
import multybot.core.CommandContext;
import multybot.core.CommandRouter;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import java.time.Instant;
import java.util.Comparator;
import java.util.Locale;
import java.util.stream.Collectors;

@ApplicationScoped
public class HelpCommand implements Command {

    @Inject CommandRouter router;

    @Override
    public String name() {
        return "help";
    }

    @Override
    public SlashCommandData slashData(Locale locale) {
        // Descripción simple; si tienes i18n, cámbiala aquí
        return Commands.slash(name(), "Show the list of commands");
    }

    @Override
    public void execute(CommandContext ctx) {
        Locale locale = Locale.ENGLISH; // o derivar del ctx si lo manejas
        var cmds = router.commands().stream()
                .sorted(Comparator.comparing(Command::name))
                .collect(Collectors.toList());

        EmbedBuilder eb = new EmbedBuilder()
                .setTitle("Available commands")
                .setTimestamp(Instant.now());

        for (Command c : cmds) {
            if ("help".equalsIgnoreCase(c.name())) continue;
            String desc;
            try {
                desc = c.slashData(locale).getDescription();
            } catch (Throwable ignored) {
                desc = c.name();
            }
            eb.addField("/" + c.name(), (desc == null || desc.isBlank()) ? "…" : desc, false);
        }

        ctx.event().replyEmbeds(eb.build()).setEphemeral(true).queue();
    }
}