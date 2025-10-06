package multybot.features.util;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import multybot.core.*;
import multybot.infra.I18n;
import multybot.infra.TimeUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.StageChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.awt.*;
import java.time.Instant;
import java.util.Locale;

@ApplicationScoped
@DiscordCommand(name = "serverinfo", descriptionKey = "serverinfo.description")
@Cooldown(seconds = 5)
public class ServerInfoCommand implements Command {

    @Inject I18n i18n;

    @Override
    public SlashCommandData slashData(Locale locale) {
        return Commands.slash("serverinfo", i18n.msg(locale, "serverinfo.description"));
    }

    @Override
    public void execute(CommandContext ctx) {
        Guild g = ctx.guild();

        int members = g.getMemberCount();
        int humans = (int) g.getMemberCache().stream().filter(m -> !m.getUser().isBot()).count();
        int bots   = Math.max(0, members - humans);

        int text   = (int) g.getChannels().stream().filter(c -> c instanceof TextChannel).count();
        int voice  = (int) g.getChannels().stream().filter(c -> c instanceof VoiceChannel).count();
        int forum  = (int) g.getChannels().stream().filter(c -> c instanceof ForumChannel).count();
        int stage  = (int) g.getChannels().stream().filter(c -> c instanceof StageChannel).count();
        int cats   = (int) g.getChannels().stream().filter(c -> c instanceof Category).count();

        String owner = g.getOwnerId() != null ? "<@" + g.getOwnerId() + ">" : i18n.msg(ctx.locale(), "serverinfo.unknown");
        String created = TimeUtil.fmtInstant(g.getTimeCreated().toInstant(), TimeUtil.DEFAULT_ZONE, ctx.locale());
        String boost = g.getBoostTier() != null ? g.getBoostTier().name() : "NONE";
        int rolesCount = g.getRoles().size();

        EmbedBuilder eb = new EmbedBuilder()
                .setTitle(i18n.msg(ctx.locale(), "serverinfo.title"))
                .setColor(new Color(0x5865F2))
                .setThumbnail(g.getIconUrl())
                .addField(i18n.msg(ctx.locale(), "serverinfo.owner"), owner, true)
                .addField(i18n.msg(ctx.locale(), "serverinfo.created"), created, true)
                .addField(i18n.msg(ctx.locale(), "serverinfo.members"),
                        i18n.msg(ctx.locale(), "serverinfo.members.humans") + ": **" + humans + "**\n" +
                                i18n.msg(ctx.locale(), "serverinfo.members.bots")   + ": **" + bots   + "**\n" +
                                "(" + members + ")", true)
                .addField(i18n.msg(ctx.locale(), "serverinfo.channels"),
                        i18n.msg(ctx.locale(), "serverinfo.channels.text") + ": **" + text + "**\n" +
                                i18n.msg(ctx.locale(), "serverinfo.channels.voice") + ": **" + voice + "**\n" +
                                i18n.msg(ctx.locale(), "serverinfo.channels.forum") + ": **" + forum + "**\n" +
                                i18n.msg(ctx.locale(), "serverinfo.channels.stage") + ": **" + stage + "**\n" +
                                i18n.msg(ctx.locale(), "serverinfo.channels.categories") + ": **" + cats + "**", true)
                .addField(i18n.msg(ctx.locale(), "serverinfo.roles"), String.valueOf(rolesCount), true)
                .addField(i18n.msg(ctx.locale(), "serverinfo.boost"), boost, true);

        ctx.hook().sendMessageEmbeds(eb.build()).queue();
    }

    @Override public String name() { return "serverinfo"; }
}
