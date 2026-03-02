package multybottest.uptime;

import multybottest.core.CommandContext;
import multybottest.ports.DiscordGateway;
import multybottest.time.TimeProvider;

import java.time.Duration;
import java.time.Instant;

public final class UptimeUseCase {

  private final DiscordGateway discord;
  private final TimeProvider clock;
  private final Instant startedAt;

  public UptimeUseCase(DiscordGateway discord, TimeProvider clock, Instant startedAt) {
    this.discord = discord;
    this.clock = clock;
    this.startedAt = startedAt;
  }

  public void handle(CommandContext ctx) {
    Duration uptime = Duration.between(startedAt, clock.now());
    discord.reply(ctx.interactionId(), UptimeFormatter.format(uptime));
  }
}
