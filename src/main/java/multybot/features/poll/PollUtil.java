package multybot.features.poll;

import multybot.infra.I18n;
import multybot.core.CommandContext;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;
import java.text.MessageFormat;
import java.util.Arrays;

public class PollUtil {
    public static EmbedBuilder resultsEmbed(CommandContext ctx, PollDoc poll) {
        int nOpts = poll.options.size();
        int[] counts = new int[nOpts];
        poll.votes.values().forEach(idx -> { if (idx>=0 && idx<nOpts) counts[idx]++; });
        int total = Arrays.stream(counts).sum();
        var eb = new EmbedBuilder()
                .setTitle("🗳️ " + poll.question + " — " + ctx.i18n().msg(ctx.locale(), "poll.results.title"))
                .setColor(new Color(0x57F287))
                .setFooter(MessageFormat.format(ctx.i18n().msg(ctx.locale(),"poll.results.totalVotes"), total));
        for (int i = 0; i < nOpts; i++) {
            double pct = total == 0 ? 0 : (counts[i] * 100.0 / total);
            String bar = progressBar(pct);
            eb.addField((i+1)+". "+poll.options.get(i), String.format("%s  **%d** (%.1f%%)", bar, counts[i], pct), false);
        }
        return eb;
    }

    private static String progressBar(double pct) {
        int blocks = (int)Math.round(pct / 10.0); // 10 segmentos
        String full = "█".repeat(Math.max(0, blocks));
        String empty = "░".repeat(Math.max(0, 10 - blocks));
        return full + empty;
    }
}
