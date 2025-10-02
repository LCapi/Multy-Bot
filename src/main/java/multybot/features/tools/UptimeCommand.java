package multybot.features.tools;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import multybot.core.*;
import multybot.infra.AppInfo;
import multybot.infra.I18n;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@ApplicationScoped
@DiscordCommand(name = "uptime", descriptionKey = "uptime.description")
@Cooldown(seconds = 5)
public class UptimeCommand implements Command {

    @Inject I18n i18n;
    @Inject AppInfo app;

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");

    @Override
    public net.dv8tion.jda.api.interactions.commands.build.SlashCommandData slashData(Locale locale) {
        return net.dv8tion.jda.api.interactions.commands.build.Commands
                .slash("uptime", i18n.msg(locale, "uptime.description"));
    }

    @Override
    public void execute(CommandContext ctx) {
        var started = app.startedAt().atZone(ZoneId.systemDefault());
        var eb = new EmbedBuilder()
                .setTitle(i18n.msg(ctx.locale(), "uptime.title"))
                .setColor(new Color(0x5865F2))
                .addField("Started", FMT.format(started), true)
                .addField("Uptime", multybot.infra.AppInfo.human(app.uptime()), true);

        ctx.hook().sendMessageEmbeds(eb.build()).queue();
    }

    @Override public String name() { return "uptime"; }
}
