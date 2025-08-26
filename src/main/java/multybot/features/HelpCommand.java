package dev.lcapi.multybot.features;

import java.util.Locale;
import dev.lcapi.multybot.core.*;
import dev.lcapi.multybot.infra.I18n;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

@ApplicationScoped
@DiscordCommand(name="help", descriptionKey="help.description")
public class HelpCommand implements Command {

    @Inject I18n i18n;
    @Inject CommandRouter router;

    @Override
    public SlashCommandData slashData(Locale locale) {
        return Commands.slash("help", i18n.msg(locale, "help.description"));
    }

    @Override
    public void execute(CommandContext ctx) {
        String list = router.help(ctx.locale());
        ctx.hook().sendMessage("**Comandos disponibles**:\n"+list).queue();
    }

    @Override public String name() { return "help"; }
}