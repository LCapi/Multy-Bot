package multybot.features.moderation;

import jakarta.enterprise.context.ApplicationScoped;
import multybot.core.*;
import multybot.infra.I18n;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import jakarta.inject.Inject;
import org.bson.types.ObjectId;

import java.util.Locale;
import java.util.Objects;

@ApplicationScoped
@DiscordCommand(name = "reason", descriptionKey = "reason.set.description")
@RequirePermissions({ Permission.KICK_MEMBERS })
@Cooldown(seconds = 3)
public class ReasonCommand implements Command {

    @Inject I18n i18n;

    @Override
    public SlashCommandData slashData(Locale locale) {
        return Commands.slash("reason", i18n.msg(locale, "reason.set.description"))
                .addSubcommands(
                        new SubcommandData("set", i18n.msg(locale, "reason.set.description"))
                                .addOption(OptionType.STRING, "id", "ID del caso (ObjectId)", true)
                                .addOption(OptionType.STRING, "reason", "Nueva razón", true)
                );
    }

    @Override
    public void execute(CommandContext ctx) {
        var ev = ctx.event();
        var sub = ev.getSubcommandName();
        if (!"set".equals(sub)) {
            ctx.hook().sendMessage("Unknown subcommand").queue();
            return;
        }
        String id = ev.getOption("id").getAsString();
        String reason = ev.getOption("reason").getAsString();

        if (!ObjectId.isValid(id)) {
            ctx.hook().sendMessage(i18n.msg(ctx.locale(), "case.error.id")).queue();
            return;
        }
        ModerationCase mc = ModerationCase.findById(new ObjectId(id));
        if (mc == null || !Objects.equals(mc.guildId, ctx.guild().getId())) {
            ctx.hook().sendMessage(i18n.msg(ctx.locale(), "reason.set.notfound")).queue();
            return;
        }

        mc.reason = reason;
        mc.update();

        ctx.hook().sendMessage(i18n.msg(ctx.locale(), "reason.set.ok")).queue();
    }

    @Override public String name() { return "reason"; }
}
