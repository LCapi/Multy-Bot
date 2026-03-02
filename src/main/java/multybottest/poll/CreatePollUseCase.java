package multybottest.poll;

import multybottest.core.CommandContext;
import multybottest.ports.DiscordGateway;

import java.util.ArrayList;
import java.util.List;

public final class CreatePollUseCase {

  private final DiscordGateway discord;

  public CreatePollUseCase(DiscordGateway discord) {
    this.discord = discord;
  }

  public void handle(CommandContext ctx) {
    String question = getOpt(ctx, "question");
    String rawOptions = getOpt(ctx, "options");

    if (question == null || question.trim().isEmpty()) {
      discord.reply(ctx.interactionId(), "Poll question cannot be empty.");
      return;
    }

    List<String> options = parseOptions(rawOptions);

    if (options.size() < PollLimits.MIN_OPTIONS) {
      discord.reply(ctx.interactionId(), "Poll must have at least " + PollLimits.MIN_OPTIONS + " options.");
      return;
    }
    if (options.size() > PollLimits.MAX_OPTIONS) {
      discord.reply(ctx.interactionId(), "Poll cannot have more than " + PollLimits.MAX_OPTIONS + " options.");
      return;
    }

    discord.reply(ctx.interactionId(), formatPoll(question.trim(), options));
  }

  private static String getOpt(CommandContext ctx, String key) {
    return (ctx.options() == null) ? null : ctx.options().get(key);
  }

  static List<String> parseOptions(String raw) {
    if (raw == null) return List.of();
    String[] parts = raw.split("\\|");
    List<String> out = new ArrayList<>();
    for (String p : parts) {
      String t = p.trim();
      if (!t.isEmpty()) out.add(t);
    }
    return out;
  }

  static String formatPoll(String question, List<String> options) {
    StringBuilder sb = new StringBuilder();
    sb.append("📊 ").append(question).append("\n");
    for (int i = 0; i < options.size(); i++) {
      sb.append(i + 1).append(") ").append(options.get(i)).append("\n");
    }
    return sb.toString();
  }
}
