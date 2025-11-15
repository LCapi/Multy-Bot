package multybot.features;

import jakarta.enterprise.context.ApplicationScoped;
import multybot.core.AbstractCommand;
import multybot.core.CommandContext;

@ApplicationScoped
public class PingCommand extends AbstractCommand {

    @Override
    public String name() {
        return "ping";
    }

    @Override
    public void execute(CommandContext ctx) {
        ctx.reply("Pong!");
    }
}