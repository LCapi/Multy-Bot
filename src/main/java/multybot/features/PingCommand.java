package multybot.features;

import jakarta.enterprise.context.ApplicationScoped;
import multybot.core.Command;
import multybot.core.CommandContext;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.Locale;

@ApplicationScoped
public class PingCommand implements Command {

    @Override
    public SlashCommandData slashData(Locale locale) {
        return Commands.slash("ping", "Measure gateway latency");
    }

    @Override
    public void execute(CommandContext ctx) {
        ctx.reply("Pong! " + ctx.jda().getGatewayPing() + " ms");
    }
}
