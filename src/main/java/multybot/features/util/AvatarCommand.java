package multybot.features.util;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import multybot.core.*;
import multybot.infra.I18n;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.awt.*;
import java.util.Locale;

@ApplicationScoped
@DiscordCommand(name = "avatar", descriptionKey = "avatar.description")
@Cooldown(seconds = 5)
public class AvatarCommand implements Command {

    @Inject I18n i18n;

    @Override
    public SlashCommandData slashData(Locale locale) {
        return Commands.slash("avatar", i18n.msg(locale, "avatar.description"))
                .addOption(OptionType.USER, "user", "Usuario (opcional)", false);
    }

    @Override
    public void execute(CommandContext ctx) {
        var ev = ctx.event();
        User targetUser = ev.getOption("user") != null ? ev.getOption("user").getAsUser() : ctx.member().getUser();
        Member targetMember = ctx.guild().getMember(targetUser); // puede ser null

        String url = targetMember != null ? targetMember.getEffectiveAvatarUrl()
                : (targetUser.getAvatarUrl() != null ? targetUser.getAvatarUrl() : targetUser.getDefaultAvatarUrl());

        String title = i18n.msg(ctx.locale(), "avatar.title", targetUser.getName());
        EmbedBuilder eb = new EmbedBuilder()
                .setTitle(title)
                .setColor(new Color(0x57F287))
                .setImage(url + "?size=1024");

        ctx.hook().sendMessageEmbeds(eb.build()).queue();
    }

    @Override public String name() { return "avatar"; }
}
