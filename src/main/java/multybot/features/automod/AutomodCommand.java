package multybot.features.automod;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import multybot.core.*;
import multybot.infra.I18n;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.build.*;

import java.util.Locale;
import net.dv8tion.jda.api.interactions.commands.OptionType;

@ApplicationScoped
@DiscordCommand(name="automod", descriptionKey="automod.description")
@RequirePermissions({ Permission.MANAGE_SERVER }) // o MANAGE_GUILD según tu JDA
@Cooldown(seconds = 5)
public class AutomodCommand implements Command {

    @Inject I18n i18n;

    @Override
    public net.dv8tion.jda.api.interactions.commands.build.SlashCommandData slashData(Locale locale) {
        return Commands.slash("automod", i18n.msg(locale, "automod.description"))
                .addSubcommands(
                        new SubcommandData("enable", i18n.msg(locale, "automod.enable.description")),
                        new SubcommandData("disable", i18n.msg(locale, "automod.disable.description"))
                );
    }

    @Override
    public void execute(CommandContext ctx) {
        String gid = ctx.guild().getId();
        var sub = ctx.event().getSubcommandName();
        AutomodConfig cfg = AutomodConfig.loadOrDefault(gid);

        switch (sub) {
            case "enable" -> {
                if (cfg.enabled) {
                    ctx.hook().sendMessage(i18n.msg(ctx.locale(),"automod.already.enabled")).queue();
                    return;
                }
                cfg.enabled = true;
                cfg.persistOrUpdate();
                ctx.hook().sendMessage(i18n.msg(ctx.locale(),"automod.enabled")).queue();
            }
            case "disable" -> {
                if (!cfg.enabled) {
                    ctx.hook().sendMessage(i18n.msg(ctx.locale(),"automod.already.disabled")).queue();
                    return;
                }
                cfg.enabled = false;
                cfg.persistOrUpdate();
                ctx.hook().sendMessage(i18n.msg(ctx.locale(),"automod.disabled")).queue();
            }
            default -> ctx.hook().sendMessage("Unknown subcommand").queue();
        }
    }

    @Override public String name() { return "automod"; }
}
