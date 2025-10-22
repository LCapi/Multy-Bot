package multybot.features.tools;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import multybot.core.*;
import multybot.infra.I18n;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.awt.*;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;

@ApplicationScoped
@DiscordCommand(name = "permcheck", descriptionKey = "permcheck.description")
@Cooldown(seconds = 5)
public class PermcheckCommand implements Command {

    @Inject I18n i18n;

    // Permisos críticos para el BOT (moderación + operar en el canal)
    private static final Permission[] BOT_CRITICAL = new Permission[] {
            Permission.VIEW_CHANNEL,
            Permission.MESSAGE_SEND,
            Permission.MESSAGE_HISTORY,
            Permission.MESSAGE_MANAGE,     // /purge
            Permission.MESSAGE_EMBED_LINKS,        // enviar embeds
            Permission.KICK_MEMBERS,
            Permission.BAN_MEMBERS,
            Permission.MODERATE_MEMBERS,   // timeouts
            Permission.MANAGE_ROLES        // join/panel roles
    };

    // Permisos relevantes para el USUARIO que ejecuta comandos de moderación
    private static final Permission[] USER_CRITICAL = new Permission[] {
            Permission.KICK_MEMBERS,
            Permission.BAN_MEMBERS,
            Permission.MODERATE_MEMBERS,
            Permission.MESSAGE_MANAGE
    };

    @Override
    public SlashCommandData slashData(Locale locale) {
        return Commands.slash("permcheck", i18n.msg(locale, "permcheck.description"));
    }

    @Override
    public void execute(CommandContext ctx) {
        var ev = ctx.event();
        GuildChannel channel = ev.getGuildChannel(); // canal actual del slash
        Member self = ctx.guild().getSelfMember();
        Member user = ctx.member();

        // Evalúa permisos en el canal actual (respetando overrides)
        Result botRes  = evaluate(self, channel, BOT_CRITICAL);
        Result usrRes  = evaluate(user, channel, USER_CRITICAL);

        // Construye embed
        String title = i18n.msg(ctx.locale(), "permcheck.title", channel.getName());
        EmbedBuilder eb = new EmbedBuilder()
                .setTitle(title)
                .setDescription(i18n.msg(ctx.locale(), "permcheck.note"));

        eb.addField(i18n.msg(ctx.locale(), "permcheck.bot"), formatLines(botRes), false);
        eb.addField(i18n.msg(ctx.locale(), "permcheck.user"), formatLines(usrRes), false);

        // Color según estado (rojo si al bot le falta algo; amarillo si al user; verde si todo OK)
        if (!botRes.missing().isEmpty()) eb.setColor(new Color(0xED4245));
        else if (!usrRes.missing().isEmpty()) eb.setColor(new Color(0xFEE75C));
        else eb.setColor(new Color(0x57F287));

        // Footer/summary
        String footer = (botRes.missing().isEmpty() && usrRes.missing().isEmpty())
                ? i18n.msg(ctx.locale(), "permcheck.ok")
                : i18n.msg(ctx.locale(), "permcheck.missing.count", botRes.missing().size() + usrRes.missing().size());
        eb.setFooter(footer);

        ctx.hook().sendMessageEmbeds(eb.build()).queue();
    }

    private record Result(List<Permission> ok, List<Permission> missing) {}

    private Result evaluate(Member member, GuildChannel channel, Permission[] critical) {
        EnumSet<Permission> present = member.getPermissions(channel);
        List<Permission> ok = new ArrayList<>();
        List<Permission> miss = new ArrayList<>();
        for (Permission p : critical) {
            if (present.contains(p)) ok.add(p); else miss.add(p);
        }
        return new Result(ok, miss);
    }

    private String formatLines(Result r) {
        StringBuilder sb = new StringBuilder();
        for (Permission p : r.ok()) {
            sb.append("✅ ").append(p.getName()).append("\n");
        }
        for (Permission p : r.missing()) {
            sb.append("**❌ ").append(p.getName()).append("**\n");
        }
        return sb.toString().isEmpty() ? "—" : sb.toString();
    }

    @Override public String name() { return "permcheck"; }
}
