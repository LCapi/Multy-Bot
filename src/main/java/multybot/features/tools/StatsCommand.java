package multybot.features.tools;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import multybot.core.*;
import multybot.infra.AppInfo;
import multybot.infra.I18n;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDAInfo;
import net.dv8tion.jda.api.sharding.ShardManager;

import java.awt.*;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Locale;

@ApplicationScoped
@DiscordCommand(name = "stats", descriptionKey = "stats.description")
@Cooldown(seconds = 5)
public class StatsCommand implements Command {

    @Inject I18n i18n;
    @Inject AppInfo app;

    @Override
    public net.dv8tion.jda.api.interactions.commands.build.SlashCommandData slashData(Locale locale) {
        return net.dv8tion.jda.api.interactions.commands.build.Commands
                .slash("stats", i18n.msg(locale, "stats.description"));
    }

    @Override
    public void execute(CommandContext ctx) {
        var jda = ctx.jda();
        int guilds = jda.getGuildCache().size();
        int users  = jda.getUserCache().size();
        long ping  = jda.getGatewayPing();

        Runtime rt = Runtime.getRuntime();
        long used  = (rt.totalMemory() - rt.freeMemory()) / (1024 * 1024);
        long total = rt.totalMemory() / (1024 * 1024);

        RuntimeMXBean mx = ManagementFactory.getRuntimeMXBean();
        String javaVer = System.getProperty("java.version");

        var eb = new EmbedBuilder()
                .setTitle(i18n.msg(ctx.locale(), "stats.title"))
                .setColor(new Color(0x57F287))
                .addField(i18n.msg(ctx.locale(), "stats.guilds"), String.valueOf(guilds), true)
                .addField(i18n.msg(ctx.locale(), "stats.users"),  String.valueOf(users),  true)
                .addField(i18n.msg(ctx.locale(), "stats.ping"),   ping + " ms", true)
                .addField(i18n.msg(ctx.locale(), "stats.mem"),    used + " / " + total + " MB", true)
                .addField(i18n.msg(ctx.locale(), "stats.uptime"), AppInfo.human(app.uptime()), true)
                .addField(i18n.msg(ctx.locale(), "stats.java"),   javaVer, true)
                .addField(i18n.msg(ctx.locale(), "stats.jda"),    JDAInfo.VERSION, true)
                .setFooter(i18n.msg(ctx.locale(), "stats.ok"));

        ctx.hook().sendMessageEmbeds(eb.build()).queue();
    }

    @Override public String name() { return "stats"; }
}
