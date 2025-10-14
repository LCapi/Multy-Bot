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
@DiscordCommand(name = "ban", descriptionKey = "mod.ban.description")
@RequirePermissions({ Permission.BAN_MEMBERS })
@Cooldown(seconds = 5)
public class BanCommand implements Command {

    @Inject I18n i18n;
    @Inject LogService logs;

    @Override
    public SlashCommandData slashData(Locale locale) {
        return Commands.slash("ban", i18n.msg(locale, "mod.ban.description"))
                .addOption(OptionType.USER, "user", "Usuario a banear", true)
                .addOption(OptionType.INTEGER, "days", "Borrar mensajes últimos N días (0-7)", false)
                .addOption(OptionType.STRING, "reason", "Razón", false);
    }

    @Override
    public void execute(CommandContext ctx) {
        var guild = ctx.guild();
        var member = ctx.event().getOption("user").getAsMember(); // o como lo estés recibiendo
        if (member != null) {
            guild.ban(member.getUser(), 1) // 1 día de purge, ajusta si hace falta
                    .reason("Manual ban")
                    .queue();
        }

        if (target == null || !ctx.guild().getSelfMember().canInteract(target)) {
            ctx.hook().sendMessage(i18n.msg(ctx.locale(), "mod.error.member.higher")).queue();
            return;
        }
        if (!ctx.guild().getSelfMember().hasPermission(Permission.BAN_MEMBERS)) {
            ctx.hook().sendMessage(i18n.msg(ctx.locale(), "mod.error.permission.bot")).queue();
            return;
        }

        guild.ban(member.getUser(), 1).reason(reason).queue();

        var mc = new ModerationCase();
        mc.guildId = ctx.guild().getId();
        mc.moderatorId = ctx.member().getId();
        mc.targetId = target.getId();
        mc.type = ModerationType.BAN;
        mc.reason = reason;
        mc.persist();

        logs.log(ctx.guild(), "**[BAN]** <@%s> (by <@%s>) — %s".formatted(
                target.getId(), ctx.member().getId(), reason));

        ctx.hook().sendMessage(i18n.msg(ctx.locale(), "mod.done")).queue();
    }

    @Override public String name() { return "ban"; }
}
