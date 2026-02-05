package multybottest.core.commands;

import multybottest.core.CommandContext;
import multybottest.ports.DiscordGateway;

public final class PingCommandService {
    private final DiscordGateway discord;

    public PingCommandService(DiscordGateway discord) {
        this.discord = discord;
    }

    public void handle(CommandContext ctx) {
        discord.reply(ctx.interactionId(), "pong");
    }
}