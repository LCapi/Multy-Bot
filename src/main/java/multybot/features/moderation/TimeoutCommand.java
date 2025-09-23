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

import java.time.Duration;
import java.util.Date;
import java.util.Locale;

@ApplicationScoped
@DiscordCommand(name = "timeout", descriptionKey = "mod.timeout.description")
@RequirePermissions({ Permission.MODERATE_MEMBERS })
@Cooldown(seconds = 5)
public class TimeoutCommand implements Command {

    @Inject I18n i18n;
    @Inject LogService logs;

    @Override
    public SlashCommandData slashData(Locale locale) {
        return Commands.slash("timeout", i18n.msg(locale, "mod.timeout.description"))
                .addOption(OptionType.USER, "user", "Usuario a silenciar", true)
                .addOption(OptionType.INTEGER, "minutes", "Minutos de silencio (1-40320)", true)
                .addOption(OptionType.STRING, "reason", "Razón", false);
    }

    @Override
    public void execute(CommandContext ctx) {
        var ev = ctx.event();
        Member target = ev.getOption("user").getAsMember();
        long minutes = ev.getOption("minutes").getAsLong();
        String reason = ev.getOption("reason") != null ? ev.getOption("reason").getAsString()
                : i18n.msg(ctx.locale(), "mod.reason");

        if (minutes < 1) minutes = 1;
        if (minutes > 40320) minutes = 40320; // 28 días

        if (target == null || !ctx.guild().getSelfMember().canInteract(target)) {
            ctx.hook().sendMessage(i18n.msg(ctx.locale(), "mod.error.member.higher")).queue();
            return;
        }
        if (!ctx.guild().getSelfMember().hasPermission(Permission.MODERATE_MEMBERS)) {
            ctx.hook().sendMessage(i18n.msg(ctx.locale(), "mod.error.permission.bot")).queue();
            return;
        }

        Duration d = Duration.ofMinutes(minutes);
        target.timeoutFor(d).reason(reason).queue();

        // Guardar caso
        var mc = new ModerationCase();
        mc.guildId = ctx.guild().getId();
        mc.moderatorId = ctx.member().getId();
        mc.targetId = target.getId();
        mc.type = ModerationType.TIMEOUT;
        mc.reason = reason;
        mc.expiresAt = new Date(System.currentTimeMillis() + d.toMillis());
        mc.persist();

        logs.log(ctx.guild(), "**[TIMEOUT]** <@%s> %d min (by <@%s>) — %s".formatted(
                target.getId(), minutes, ctx.member().getId(), reason));

        ctx.hook().sendMessage(i18n.msg(ctx.locale(), "mod.done")).queue();
    }

    @Override public String name() { return "timeout"; }
}
