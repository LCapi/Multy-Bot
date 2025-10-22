package multybot.features.tools;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import multybot.core.*;
import multybot.infra.I18n;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.awt.Color;
import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.util.Locale;

@ApplicationScoped
@DiscordCommand(name = "stats", descriptionKey = "stats.description")
@Cooldown(seconds = 3)
public class StatsCommand implements Command {

    @Inject I18n i18n;

    private static final long START_MS = System.currentTimeMillis();

    @Override
    public SlashCommandData slashData(Locale locale) {
        return Commands.slash("stats", i18n.msg(locale, "stats.description"));
    }

    @Override
    public void execute(CommandContext ctx) {
        long totalMb = Runtime.getRuntime().totalMemory() / (1024 * 1024);
        long freeMb  = Runtime.getRuntime().freeMemory()  / (1024 * 1024);
        long usedMb  = totalMb - freeMb;

        long uptimeMs = System.currentTimeMillis() - START_MS;
        Duration d = Duration.ofMillis(uptimeMs);
        String upStr = "%dd %02d:%02d:%02d".formatted(d.toDaysPart(), d.toHoursPart(), d.toMinutesPart(), d.toSecondsPart());

        long guilds = ctx.jda().getGuilds().size();
        long users  = ctx.jda().getUsers().size();

        var eb = new EmbedBuilder()
                .setTitle(i18n.msg(ctx.locale(), "stats.title"))
                .setColor(new Color(0x5865F2))
                .addField("Guilds", String.valueOf(guilds), true)
                .addField("Users", String.valueOf(users), true)
                .addField("Memory", usedMb + " / " + totalMb + " MB", false)
                .addField("Uptime", upStr, false)
                .setFooter("JVM: " + ManagementFactory.getRuntimeMXBean().getVmName()
                        + " " + System.getProperty("java.version"));

        ctx.hook().sendMessageEmbeds(eb.build()).queue();
    }

    @Override public String name() { return "stats"; }
}
