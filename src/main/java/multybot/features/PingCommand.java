package multybot.features;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import multybot.core.AbstractCommand;
import multybot.core.CommandContext;
import multybot.core.DiscordCommand;
import multybot.infra.I18n;

@ApplicationScoped
@DiscordCommand(name = "ping", descriptionKey = "ping.description")
public class PingCommand extends AbstractCommand {

    @Inject
    I18n i18n;

    @Override
    public String name() {
        return "ping";
    }

    @Override
    public void execute(CommandContext ctx) {
        long gatewayPing = ctx.jda().getGatewayPing();
        String msg = i18n.msg(ctx.locale(), "ping.reply", gatewayPing);
        ctx.replyEphemeral(msg);
    }
}