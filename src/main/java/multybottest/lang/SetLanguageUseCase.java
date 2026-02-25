package multybottest.lang;

import multybottest.core.CommandContext;
import multybottest.ports.DiscordGateway;

import java.util.Arrays;
import java.util.stream.Collectors;

public final class SetLanguageUseCase {

  private final DiscordGateway discord;
  private final LanguageRepository repo;

  public SetLanguageUseCase(DiscordGateway discord, LanguageRepository repo) {
    this.discord = discord;
    this.repo = repo;
  }

  public void handle(CommandContext ctx) {
    String guildId = ctx.guildId();
    String raw = (ctx.options() == null) ? null : ctx.options().get("lang");

    // If no param => show current + available
    if (raw == null || raw.trim().isEmpty()) {
      Language current = repo.getLanguage(guildId);
      if (current == null) current = Language.EN;

      String available = Arrays.stream(Language.values())
          .map(Language::code)
          .collect(Collectors.joining(", "));

      discord.reply(ctx.interactionId(),
          "Current language: " + current.code() + "\n" +
          "Available: " + available);
      return;
    }

    Language lang = Language.fromCode(raw);
    if (lang == null) {
      String available = Arrays.stream(Language.values())
          .map(Language::code)
          .collect(Collectors.joining(", "));
      discord.reply(ctx.interactionId(),
          "Unsupported language: " + raw.trim() + "\n" +
          "Available: " + available);
      return;
    }

    repo.setLanguage(guildId, lang);
    discord.reply(ctx.interactionId(), "Language set to: " + lang.code());
  }
}
