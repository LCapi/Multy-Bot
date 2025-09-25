package multybot.features.moderation;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import multybot.core.*;
import multybot.infra.I18n;
import multybot.infra.LogService;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.Locale;

@ApplicationScoped
@DiscordCommand(name = "kick", descriptionKey = "mod.kick.description")
@RequirePermissions({ Permission.KICK_MEMBERS })
@Cooldown(seconds = 5)
public class KickCommand implements Command {

    @Inject I18n i18n;
    @Inject LogService logs;

    @Override
    public SlashCommandData slashData(Locale locale) {
        return Commands.slash("kick", i18n.msg(locale, "mod.kick.description"))
                .addOption(OptionType.USER, "user", "Usuario a expulsar", true)
                .addOption(OptionType.STRING, "reason", "Razón", false);
    }

    @Override
    public void execute(CommandContext ctx) {
        var ev = ctx.event();
        Member target = ev.getOption("user").getAsMember();
        String reason = ev.getOption("reason") != null ? ev.getOption("reason").getAsString()
                : i18n.msg(ctx.locale(), "mod.reason");

        if (target == null || !ctx.guild().getSelfMember().canInteract(target)) {
            ctx.hook().sendMessage(i18n.msg(ctx.locale(), "mod.error.member.higher")).queue();
            return;
        }
        if (!ctx.guild().getSelfMember().hasPermission(Permission.KICK_MEMBERS)) {
            ctx.hook().sendMessage(i18n.msg(ctx.locale(), "mod.error.permission.bot")).queue();
            return;
        }

        target.kick().reason(reason).queue();

        var mc = new ModerationCase();
        mc.guildId = ctx.guild().getId();
        mc.moderatorId = ctx.member().getId();
        mc.targetId = target.getId();
        mc.type = ModerationType.KICK;
        mc.reason = reason;
        mc.persist();

        logs.log(ctx.guild(), "**[KICK]** <@%s> (by <@%s>) — %s".formatted(
                target.getId(), ctx.member().getId(), reason));

        ctx.hook().sendMessage(i18n.msg(ctx.locale(), "mod.done")).queue();
    }

    @Override public String name() { return "kick"; }
}
