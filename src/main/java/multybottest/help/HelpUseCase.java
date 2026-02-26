package multybottest.help;

import multybottest.core.CommandContext;
import multybottest.ports.DiscordGateway;

import java.util.Comparator;

public final class HelpUseCase {

  private final DiscordGateway discord;
  private final CommandCatalog catalog;

  public HelpUseCase(DiscordGateway discord, CommandCatalog catalog) {
    this.discord = discord;
    this.catalog = catalog;
  }

  public void handle(CommandContext ctx) {
    var out = new StringBuilder("Available commands:\n");

    catalog.list().stream()
        .sorted(Comparator.comparing(CommandDescriptor::name, String.CASE_INSENSITIVE_ORDER))
        .forEach(c -> out.append("/")
            .append(c.name())
            .append(" - ")
            .append(c.description())
            .append("\n"));

    discord.reply(ctx.interactionId(), out.toString());
  }
}
