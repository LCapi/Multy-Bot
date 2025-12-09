package multybot.features.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import multybot.core.Command;
import multybot.core.CommandContext;
import multybot.core.Cooldown;
import multybot.core.DiscordCommand;
import multybot.infra.I18n;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.Locale;

@ApplicationScoped
@DiscordCommand(name = "lang", descriptionKey = "lang.description")
@Cooldown(seconds = 3)
// Si en el futuro quieres limitarlo a admins:
// @RequirePermissions({ Permission.MANAGE_SERVER })
public class LangCommand implements Command {

    @Inject
    I18n i18n;

    @Override
    public String name() {
        return "lang";
    }

    @Override
    public SlashCommandData slashData(Locale locale) {
        // Comando simple sin subcomandos: solo informa del idioma actual
        return Commands.slash(
                name(),
                i18n.msg(locale, "lang.description")
        );
    }

    @Override
    public void execute(CommandContext ctx) {
        Locale effective = ctx.locale();
        String code = effective.getLanguage(); // "es", "en", etc.

        String reply = i18n.msg(
                effective,
                "lang.info",
                code
        );

        ctx.replyEphemeral(reply);
    }
}