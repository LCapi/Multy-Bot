package multybottest.core;

import multybottest.ports.DiscordGateway;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class CommandDispatcher {

    private final Map<String, CommandHandler> handlers;
    private final DiscordGateway discord;

    public CommandDispatcher(List<CommandHandler> handlers, DiscordGateway discord) {
        this.handlers = handlers.stream()
                .collect(Collectors.toUnmodifiableMap(
                        h -> h.name().toLowerCase(),
                        Function.identity()
                ));
        this.discord = discord;
    }

    public void dispatch(String commandName, CommandContext ctx) {
        var handler = handlers.get(commandName.toLowerCase());
        if (handler == null) {
            discord.reply(ctx.interactionId(), "Unknown command: " + commandName);
            return;
        }
        handler.handle(ctx);
    }
}