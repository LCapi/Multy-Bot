package multybot.features.tools;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import multybot.core.AbstractCommand;
import multybot.core.CommandContext;
import multybot.core.DiscordCommand;
import multybot.infra.I18n;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;

@ApplicationScoped
@DiscordCommand(name = "uptime", descriptionKey = "uptime.description")
public class UptimeCommand extends AbstractCommand {

    @Inject I18n i18n;

    // Momento en el que se inició el bot
    private static final Instant START_TIME = Instant.now();

    @Override
    public String name() {
        return "uptime";
    }

    @Override
    public SlashCommandData slashData(Locale locale) {
        return Commands.slash(
                name(),
                i18n.msg(locale, "uptime.description")
        );
    }

    @Override
    public void execute(CommandContext ctx) {
        Duration d = Duration.between(START_TIME, Instant.now());

        long days    = d.toDays();
        long hours   = d.toHoursPart();
        long minutes = d.toMinutesPart();
        long seconds = d.toSecondsPart();

        String reply = i18n.msg(
                ctx.locale(),
                "uptime.reply",
                days, hours, minutes, seconds
        );

        ctx.replyEphemeral(reply);
    }
}