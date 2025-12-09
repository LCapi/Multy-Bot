package multybot.features.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import multybot.core.Command;
import multybot.core.CommandContext;
import multybot.core.Cooldown;
import multybot.core.DiscordCommand;
import multybot.core.RequirePermissions;
import multybot.infra.I18n;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.Locale;

@ApplicationScoped
@DiscordCommand(name = "config", descriptionKey = "config.description")
@RequirePermissions({ Permission.MANAGE_SERVER }) // o MANAGE_GUILD según tu JDA
@Cooldown(seconds = 3)
public class ConfigCommand implements Command {

    @Inject
    I18n i18n;

    @Override
    public String name() {
        return "config";
    }

    @Override
    public SlashCommandData slashData(Locale locale) {
        // Descripción deja claro que está deshabilitado por ahora
        return Commands.slash(
                name(),
                i18n.msg(locale, "config.description")
        );
    }

    @Override
    public void execute(CommandContext ctx) {
        // Implementación temporal: sin Mongo, sin GuildConfig
        String msg = i18n.msg(
                ctx.locale(),
                "config.disabled",
                ctx.guild() != null ? ctx.guild().getName() : "this server"
        );
        ctx.replyEphemeral(msg);
    }
}