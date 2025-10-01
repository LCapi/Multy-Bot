package multybot.features.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import multybot.core.*;
import multybot.infra.I18n;
import multybot.infra.persistence.GuildConfig;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@ApplicationScoped
@DiscordCommand(name = "lang", descriptionKey = "lang.description")
@Cooldown(seconds = 3)
// Si quieres restringirlo a admins del servidor, descomenta y ajusta el permiso:
// @RequirePermissions({ Permission.MANAGE_SERVER }) // En algunas versiones es MANAGE_GUILD
public class LangCommand implements Command {

    @Inject I18n i18n;

    private static final Map<String, String> DISPLAY = Map.of(
            "es", "Español",
            "en", "English"
    );

    @Override
    public SlashCommandData slashData(Locale locale) {
        return Commands.slash("lang", i18n.msg(locale, "lang.description"))
                .addSubcommands(
                        new SubcommandData("set", i18n.msg(locale, "lang.set.description"))
                                .addOption(OptionType.STRING, "locale", "es o en", true)
                );
    }

    @Override
    public void execute(CommandContext ctx) {
        var ev = ctx.event();
        var sub = ev.getSubcommandName();
        if (!Objects.equals(sub, "set")) {
            ctx.hook().sendMessage("Unknown subcommand").queue();
            return;
        }

        String code = ev.getOption("locale").getAsString().toLowerCase(Locale.ROOT).trim();
        if (!code.equals("es") && !code.equals("en")) {
            ctx.hook().sendMessage(i18n.msg(ctx.locale(), "lang.set.invalid")).queue();
            return;
        }

        // Persistir en GuildConfig
        String gid = ctx.guild().getId();
        GuildConfig cfg = GuildConfig.findById(gid);
        if (cfg == null) {
            cfg = new GuildConfig();
            cfg.guildId = gid;
        }
        cfg.locale = code;
        cfg.persistOrUpdate();

        // Responder ya en el nuevo idioma
        Locale newLocale = new Locale(code);
        String display = DISPLAY.getOrDefault(code, code);
        ctx.hook().sendMessage(i18n.msg(newLocale, "lang.set.ok", display)).queue();
    }

    @Override public String name() { return "lang"; }
}
