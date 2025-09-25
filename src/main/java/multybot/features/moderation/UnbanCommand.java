package multybot.features.moderation;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import multybot.core.*;
import multybot.infra.I18n;
import multybot.infra.LogService;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ApplicationScoped
@DiscordCommand(name = "unban", descriptionKey = "mod.unban.description")
@RequirePermissions({ Permission.BAN_MEMBERS })
@Cooldown(seconds = 5)
public class UnbanCommand implements Command {

    @Inject I18n i18n;
    @Inject LogService logs;

    private static final Pattern MENTION = Pattern.compile("<@!?(\\d+)>");

    @Override
    public SlashCommandData slashData(Locale locale) {
        return Commands.slash("unban", i18n.msg(locale, "mod.unban.description"))
                // Usamos STRING para aceptar ID o mención de usuario
                .addOption(OptionType.STRING, "user", "ID del usuario (o mención)", true)
                .addOption(OptionType.STRING, "reason", "Razón del desbaneo", false);
    }

    @Override
    public void execute(CommandContext ctx) {
        var ev = ctx.event();
        String input = ev.getOption("user").getAsString();
        String reason = ev.getOption("reason") != null
                ? ev.getOption("reason").getAsString()
                : i18n.msg(ctx.locale(), "mod.reason");

        String userId = extractUserId(input);
        if (userId == null) {
            ctx.hook().sendMessage(i18n.msg(ctx.locale(), "mod.error.userId.invalid")).queue();
            return;
        }

        // Verificar permiso del BOT
        if (!ctx.guild().getSelfMember().hasPermission(Permission.BAN_MEMBERS)) {
            ctx.hook().sendMessage(i18n.msg(ctx.locale(), "mod.error.permission.bot")).queue();
            return;
        }

        ctx.guild().unban(UserSnowflake.fromId(userId)).reason(reason).queue(
                ok -> {
                    // Guardar caso
                    ModerationCase mc = new ModerationCase();
                    mc.guildId = ctx.guild().getId();
                    mc.moderatorId = ctx.member().getId();
                    mc.targetId = userId;
                    mc.type = ModerationType.UNBAN;
                    mc.reason = reason;
                    mc.persist();

                    logs.log(ctx.guild(), "**[UNBAN]** <@" + userId + "> (by <@" + ctx.member().getId() + ">) — " + reason);
                    ctx.hook().sendMessage(i18n.msg(ctx.locale(), "mod.done")).queue();
                },
                err -> {
                    if (err instanceof ErrorResponseException ere && ere.getErrorResponse() == ErrorResponse.UNKNOWN_BAN) {
                        ctx.hook().sendMessage(i18n.msg(ctx.locale(), "mod.error.unban.notbanned")).queue();
                    } else {
                        // Error genérico (permiso/hierarquía/otro)
                        ctx.hook().sendMessage(i18n.msg(ctx.locale(), "mod.error.permission.bot")).queue();
                    }
                }
        );
    }

    @Override public String name() { return "unban"; }

    private String extractUserId(String input) {
        if (input == null) return null;
        input = input.trim();
        Matcher m = MENTION.matcher(input);
        if (m.matches()) return m.group(1);
        // Si son solo dígitos, asumimos que es un ID
        return input.chars().allMatch(Character::isDigit) ? input : null;
    }
}
