package multybot.features.moderation;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import multybot.core.*;
import multybot.infra.I18n;
import multybot.infra.LogService;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
@DiscordCommand(name = "ban", descriptionKey = "ban.description")
@RequirePermissions({ Permission.BAN_MEMBERS })
@Cooldown(seconds = 3)
public class BanCommand implements Command {

    @Inject I18n i18n;
    @Inject LogService logs;
    @Inject ModerationCaseRegistry cases;

    @Override
    public SlashCommandData slashData(Locale locale) {
        return Commands.slash("ban", i18n.msg(locale, "ban.description"))
                .addOption(OptionType.USER, "user", i18n.msg(locale, "ban.user"), true)
                .addOption(OptionType.INTEGER, "days", i18n.msg(locale, "ban.days"), false) // 0..7
                .addOption(OptionType.STRING, "reason", i18n.msg(locale, "ban.reason"), false);
    }

    @Override
    public void execute(CommandContext ctx) {
        var optUser = ctx.event().getOption("user");
        if (optUser == null) { ctx.hook().sendMessage("Missing user").queue(); return; }
        final User user = optUser.getAsUser();

        // Precalcular como 'final' para que sean capturables por las lambdas
        final int deleteDays = (ctx.event().getOption("days") != null)
                ? (int) ctx.event().getOption("days").getAsLong()
                : 1;
        final String reason = (ctx.event().getOption("reason") != null)
                ? ctx.event().getOption("reason").getAsString()
                : "Ban";

        // Permisos y jerarquía
        final Member self = ctx.guild().getSelfMember();
        final Member targetMember = ctx.guild().getMember(user);
        if (targetMember != null && !self.canInteract(targetMember)) {
            ctx.hook().sendMessage(i18n.msg(ctx.locale(), "mod.error.member.higher")).queue();
            return;
        }
        if (!self.hasPermission(Permission.BAN_MEMBERS)) {
            ctx.hook().sendMessage(i18n.msg(ctx.locale(), "mod.error.permission.bot")).queue();
            return;
        }

        // JDA 5: usar firma con TimeUnit y 'reason(...)'
        ctx.guild().ban(user, deleteDays, TimeUnit.DAYS)
                .reason(reason)
                .queue(
                        ok -> {
                            ModerationCase mc = new ModerationCase();
                            mc.guildId = ctx.guild().getId();
                            mc.moderatorId = ctx.member().getId();
                            mc.targetId = user.getId();
                            mc.type = ModerationType.BAN;
                            mc.reason = reason;
                            cases.save(mc);

                            logs.log(ctx.guild(), "**[BAN]** <@%s> (by <@%s>) — %s".formatted(
                                    user.getId(), ctx.member().getId(), reason));
                            ctx.hook().sendMessage(i18n.msg(ctx.locale(), "mod.done")).queue();
                        },
                        err -> ctx.hook().sendMessage("❌ " + err.getMessage()).queue()
                );
    }

    @Override public String name() { return "ban"; }
}
