package multybot.features;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import multybot.core.*;
import multybot.infra.I18n;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import java.util.Locale;

@ApplicationScoped
public class HelpCommand extends AbstractCommand {

    @Inject
    CommandRouter router;

    @Inject
    I18n i18n;

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
        var locale = ctx.locale();

        StringBuilder sb = new StringBuilder();
        sb.append(i18n.msg(locale, "help.header")).append("\n\n");

        for (Command cmd : router.allCommands()) {
            sb.append("/")
                    .append(cmd.name())
                    .append(" - ")
                    .append(cmd.description(locale))
                    .append("\n");
        }

        ctx.replyEphemeral(sb.toString());
    }
}