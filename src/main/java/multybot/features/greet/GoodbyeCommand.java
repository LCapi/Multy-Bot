package multybot.features.greet;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import multybot.core.*;
import multybot.infra.I18n;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.*;

import java.util.Locale;

@ApplicationScoped
@DiscordCommand(name="goodbye", descriptionKey="goodbye.set.description")
@RequirePermissions({ Permission.MANAGE_SERVER })
@Cooldown(seconds = 5)
public class GoodbyeCommand implements Command {

    @Inject I18n i18n;

    @Override
    public SlashCommandData slashData(Locale locale) {
        return Commands.slash("goodbye", i18n.msg(locale, "goodbye.set.description"))
                .addSubcommands(
                        new SubcommandData("set-channel", i18n.msg(locale, "goodbye.set.channel.description"))
                                .addOption(OptionType.CHANNEL, "channel", "Canal para despedida", true),
                        new SubcommandData("set-message", i18n.msg(locale, "goodbye.set.message.description"))
                                .addOption(OptionType.STRING, "message", "Plantilla (usa {user} y {guild})", true)
                );
    }

    @Override
    public void execute(CommandContext ctx) {
        var sub = ctx.event().getSubcommandName();
        switch (sub) {
            case "set-channel" -> setChannel(ctx);
            case "set-message" -> setMessage(ctx);
            default -> ctx.hook().sendMessage("Unknown subcommand").queue();
        }
    }

    private void setChannel(CommandContext ctx) {
        var ch = ctx.event().getOption("channel").getAsChannel().asGuildMessageChannel();
        GreetConfig cfg = GreetConfig.of(ctx.guild().getId());
        cfg.goodbyeChannelId = ch.getId();
        cfg.persistOrUpdate();
        ctx.hook().sendMessage(i18n.msg(ctx.locale(), "goodbye.set.channel.ok")).queue();
    }

    private void setMessage(CommandContext ctx) {
        String msg = ctx.event().getOption("message").getAsString();
        GreetConfig cfg = GreetConfig.of(ctx.guild().getId());
        cfg.goodbyeMessage = msg;
        cfg.persistOrUpdate();
        ctx.hook().sendMessage(i18n.msg(ctx.locale(), "goodbye.set.message.ok")).queue();
    }

    @Override public String name() { return "goodbye"; }
}
