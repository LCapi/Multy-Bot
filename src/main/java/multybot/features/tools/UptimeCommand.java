package multybot.features.tools;

import jakarta.enterprise.context.ApplicationScoped;
import multybot.core.AbstractCommand;
import multybot.core.CommandContext;
import multybot.core.DiscordCommand;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;

@ApplicationScoped
@DiscordCommand(name = "uptime", descriptionKey = "uptime.description")
public class UptimeCommand extends AbstractCommand {

    // Momento en el que se cargó la clase (inicio del bot)
    private static final Instant START_TIME = Instant.now();

    @Override
    public String name() {
        return "uptime";
    }

    @Override
    public String description(Locale locale) {
        return "Muestra el tiempo que lleva encendido el bot.";
    }

    @Override
    public SlashCommandData slashData(Locale locale) {
        return Commands.slash(name(), description(locale));
    }

    @Override
    public void execute(CommandContext ctx) {
        Duration d = Duration.between(START_TIME, Instant.now());

        long days    = d.toDays();
        long hours   = d.toHoursPart();
        long minutes = d.toMinutesPart();
        long seconds = d.toSecondsPart();

        String text = String.format(
                "Uptime: %d días, %d horas, %d minutos, %d segundos.",
                days, hours, minutes, seconds
        );

        // Lo mando efímero para no ensuciar el canal
        ctx.replyEphemeral(text);
    }
}