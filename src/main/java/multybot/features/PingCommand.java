package multybot.features;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import multybot.core.AbstractCommand;
import multybot.core.CommandContext;
import multybot.core.Cooldown;
import multybot.core.DiscordCommand;
import multybot.i18n.MessageBundle;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.Locale;

@ApplicationScoped
@DiscordCommand(name = "ping", descriptionKey = "ping.description")
@Cooldown()
public class PingCommand extends AbstractCommand {

    @Inject
    MessageBundle messages;

    @Override
    public String name() {
        return "ping";
    }

    @Override
    public String description(Locale locale) {
        return messages.get(locale, "ping.description");
    }

    @Override
    public SlashCommandData slashData(Locale locale) {
        return Commands.slash(name(), description(locale));
    }

    @Override
    public void execute(CommandContext ctx) {
        long ping = ctx.jda().getGatewayPing();
        String reply = messages.get(ctx.locale(), "ping.reply", ping);
        ctx.reply(reply);
    }
}