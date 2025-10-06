package multybot.features.util;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import multybot.core.*;
import multybot.infra.I18n;
import multybot.infra.TimeUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@ApplicationScoped
@DiscordCommand(name = "userinfo", descriptionKey = "userinfo.description")
@Cooldown(seconds = 5)
public class UserInfoCommand implements Command {

    @Inject I18n i18n;

    @Override
    public SlashCommandData slashData(Locale locale) {
        return Commands.slash("userinfo", i18n.msg(locale, "userinfo.description"))
                .addOption(OptionType.USER, "user", "Usuario (opcional)", false);
    }

    @Override
    public void execute(CommandContext ctx) {
        var ev = ctx.event();
        User targetUser = ev.getOption("user") != null ? ev.getOption("user").getAsUser() : ctx.member().getUser();
        Member targetMember = ctx.guild().getMember(targetUser); // puede ser null si no está en el guild

        String title = i18n.msg(ctx.locale(), "userinfo.title") + " — " + targetUser.getName();
        String idStr = String.valueOf(targetUser.getIdLong());
        String created = TimeUtil.fmtInstant(targetUser.getTimeCreated().toInstant(), TimeUtil.DEFAULT_ZONE, ctx.locale());
        String nick = targetMember != null && targetMember.getNickname() != null ? targetMember.getNickname() : "—";
        String joined = targetMember != null && targetMember.getTimeJoined() != null
                ? TimeUtil.fmtInstant(targetMember.getTimeJoined().toInstant(), TimeUtil.DEFAULT_ZONE, ctx.locale())
                : i18n.msg(ctx.locale(), "userinfo.not_in_guild");

        String rolesField;
        if (targetMember != null) {
            List<Role> roles = targetMember.getRoles(); // ordenados de mayor a menor
            String joinedRoles = roles.isEmpty()
                    ? "—"
                    : roles.stream().map(r -> "<@&" + r.getId() + ">").collect(Collectors.joining(" "));
            rolesField = joinedRoles.length() > 1024 ? joinedRoles.substring(0, 1021) + "..." : joinedRoles;
        } else {
            rolesField = i18n.msg(ctx.locale(), "userinfo.not_in_guild");
        }

        String thumb = targetMember != null ? targetMember.getEffectiveAvatarUrl()
                : (targetUser.getAvatarUrl() != null ? targetUser.getAvatarUrl() : targetUser.getDefaultAvatarUrl());

        EmbedBuilder eb = new EmbedBuilder()
                .setTitle(title)
                .setColor(new Color(0xFEE75C))
                .setThumbnail(thumb)
                .addField(i18n.msg(ctx.locale(), "userinfo.id"), idStr, true)
                .addField(i18n.msg(ctx.locale(), "userinfo.nick"), nick, true)
                .addField(i18n.msg(ctx.locale(), "userinfo.created"), created, true)
                .addField(i18n.msg(ctx.locale(), "userinfo.joined"), joined, true)
                .addField(i18n.msg(ctx.locale(), "userinfo.roles"), rolesField, false);

        ctx.hook().sendMessageEmbeds(eb.build()).queue();
    }

    @Override public String name() { return "userinfo"; }
}
