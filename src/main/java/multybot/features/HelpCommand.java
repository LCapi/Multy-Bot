package multybot.features;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import multybot.core.*;
import multybot.infra.I18n;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.Locale;

@ApplicationScoped
public class HelpCommand extends AbstractCommand {

    @Inject CommandRouter router;
    @Inject I18n i18n;

    @Override
    public String name() {
        return "help";
    }

    @Override
    public SlashCommandData slashData(Locale locale) {
        return Commands.slash(name(), i18n.msg(locale, "help.description"));
    }

    @Override
    public void execute(CommandContext ctx) {
        Locale locale = ctx.locale();

        StringBuilder sb = new StringBuilder();
        sb.append(i18n.msg(locale, "help.header")).append("\n\n");

        for (Command cmd : router.allCommands()) {
            String desc = descriptionFor(cmd, locale);
            sb.append("/")
                    .append(cmd.name())
                    .append(" - ")
                    .append(desc)
                    .append("\n");
        }

        ctx.replyEphemeral(sb.toString());
    }

    private String descriptionFor(Command cmd, Locale locale) {
        // Prefer annotation key if present
        DiscordCommand meta = cmd.getClass().getAnnotation(DiscordCommand.class);
        if (meta != null && meta.descriptionKey() != null && !meta.descriptionKey().isBlank()) {
            return i18n.msg(locale, meta.descriptionKey());
        }

        // Fallback (so help never breaks)
        return i18n.msg(locale, "help.no_description");
    }
}