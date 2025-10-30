package multybot.features;

import jakarta.enterprise.context.ApplicationScoped;
import multybot.core.Command;
import multybot.core.CommandContext;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;

@ApplicationScoped
public class UptimeCommand implements Command {

    private final Instant start = Instant.now();

    @Override
    public SlashCommandData slashData(Locale locale) {
        return Commands.slash("uptime", "Show bot uptime");
    }

    @Override
    public boolean isLongRunning() {
        return true; // Forzamos defer para ejemplificar
    }

    @Override
    public void execute(CommandContext ctx) {
        var d = Duration.between(start, Instant.now());
        var text = String.format("Uptime: %dh %dm %ds", d.toHours(), d.toMinutesPart(), d.toSecondsPart());
        ctx.reply(text); // Como hubo defer, edita el original
    }
}
