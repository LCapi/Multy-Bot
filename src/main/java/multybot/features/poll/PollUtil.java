package multybot.features.poll;

import multybot.core.CommandContext;
import multybot.infra.I18n;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.Color;
import java.text.MessageFormat;

public class PollUtil {

    public static EmbedBuilder resultsEmbed(CommandContext ctx, PollDoc poll) {
        int nOpts = poll.options.size();
        int[] counts = new int[nOpts];
        poll.votes.values().forEach(idx -> { if (idx >= 0 && idx < nOpts) counts[idx]++; });

        int total = 0;
        for (int c : counts) total += c;

        I18n i18n = new I18n();

        EmbedBuilder eb = new EmbedBuilder()
                .setColor(new Color(0x5865F2))
                .setTitle("🗳️ " + poll.question + " — " + i18n.msg(ctx.locale(), "poll.results.title"))
                .setFooter(MessageFormat.format(i18n.msg(ctx.locale(), "poll.results.totalVotes"), total));

        for (int i = 0; i < nOpts; i++) {
            double pct = total == 0 ? 0 : (counts[i] * 100.0 / total);
            String bar = progressBar(pct);
            eb.addField(
                    (i + 1) + ". " + poll.options.get(i),
                    String.format("%s  **%d** (%.1f%%)", bar, counts[i], pct),
                    false
            );
        }
        return eb;
    }

    private static String progressBar(double pct) {
        int blocks = (int) Math.round(pct / 10.0); // 10 segmentos
        String full = "█".repeat(Math.max(0, blocks));
        String empty = "░".repeat(Math.max(0, 10 - blocks));
        return full + empty;
    }
}
