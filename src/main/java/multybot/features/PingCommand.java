package multybot.features;

import java.util.Locale;
import multybot.core.*;
import multybot.infra.I18n;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

@ApplicationScoped
@DiscordCommand(name="ping", descriptionKey="ping.description")
@Cooldown(seconds=5)
public class PingCommand implements Command {

    @Inject I18n i18n;

    @Override
    public SlashCommandData slashData(Locale locale) {
        return Commands.slash("ping", i18n.msg(locale, "ping.description"));
    }

    @Override
    public void execute(CommandContext ctx) {
        long ws = ctx.jda().getGatewayPing();
        ctx.hook().sendMessage(i18n.msg(ctx.locale(), "ping.reply", ws)).queue();
    }

    @Override public String name() { return "ping"; }
}