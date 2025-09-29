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
@DiscordCommand(name = "untimeout", descriptionKey = "mod.untimeout.description")
@RequirePermissions({ Permission.MODERATE_MEMBERS })
@Cooldown(seconds = 5)
public class UntimeoutCommand implements Command {

    @Inject I18n i18n;
    @Inject LogService logs;

    @Override
    public SlashCommandData slashData(Locale locale) {
        return Commands.slash("untimeout", i18n.msg(locale, "mod.untimeout.description"))
                .addOption(OptionType.USER, "user", "Usuario al que retirar el silencio", true)
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
        if (!ctx.guild().getSelfMember().hasPermission(Permission.MODERATE_MEMBERS)) {
            ctx.hook().sendMessage(i18n.msg(ctx.locale(), "mod.error.permission.bot")).queue();
            return;
        }

        // Retirar timeout (si no tenía, es idempotente)
        target.removeTimeout().reason(reason).queue();

        logs.log(ctx.guild(), "**[UNTIMEOUT]** <@%s> (by <@%s>) — %s"
                .formatted(target.getId(), ctx.member().getId(), reason));

        ctx.hook().sendMessage(i18n.msg(ctx.locale(), "mod.done")).queue();
    }

    @Override public String name() { return "untimeout"; }
}
