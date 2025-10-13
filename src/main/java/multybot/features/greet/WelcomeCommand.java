package multybot.features.greet;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import multybot.core.*;
import multybot.infra.I18n;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.*;

import java.awt.*;
import java.util.Locale;

@ApplicationScoped
@DiscordCommand(name="welcome", descriptionKey="welcome.set.description")
@RequirePermissions({ Permission.MANAGE_SERVER }) // o MANAGE_GUILD
@Cooldown(seconds = 5)
public class WelcomeCommand implements Command {

    @Inject I18n i18n;

    @Override
    public SlashCommandData slashData(Locale locale) {
        return Commands.slash("welcome", i18n.msg(locale, "welcome.set.description"))
                .addSubcommands(
                        new SubcommandData("set-channel", i18n.msg(locale, "welcome.set.channel.description"))
                                .addOption(OptionType.CHANNEL, "channel", "Canal para bienvenida", true),
                        new SubcommandData("set-message", i18n.msg(locale, "welcome.set.message.description"))
                                .addOption(OptionType.STRING, "message", "Plantilla (usa {user} y {guild})", true),
                        new SubcommandData("set-image", i18n.msg(locale, "welcome.set.image.description"))
                                .addOption(OptionType.STRING, "url", "URL de imagen (opcional)", true),
                        new SubcommandData("test", i18n.msg(locale, "welcome.test.description"))
                );
    }

    @Override
    public void execute(CommandContext ctx) {
        var sub = ctx.event().getSubcommandName();
        switch (sub) {
            case "set-channel" -> setChannel(ctx);
            case "set-message" -> setMessage(ctx);
            case "set-image"   -> setImage(ctx);
            case "test"        -> test(ctx);
            default -> ctx.hook().sendMessage("Unknown subcommand").queue();
        }
    }

    private void setChannel(CommandContext ctx) {
        var opt = ctx.event().getOption("channel");
        var ch  = opt.getAsChannel().asGuildMessageChannel();
        GreetConfig cfg = GreetConfig.of(ctx.guild().getId());
        cfg.welcomeChannelId = ch.getId();
        cfg.persistOrUpdate();
        ctx.hook().sendMessage(i18n.msg(ctx.locale(), "welcome.set.channel.ok")).queue();
    }

    private void setMessage(CommandContext ctx) {
        String msg = ctx.event().getOption("message").getAsString();
        GreetConfig cfg = GreetConfig.of(ctx.guild().getId());
        cfg.welcomeMessage = msg;
        cfg.persistOrUpdate();
        ctx.hook().sendMessage(i18n.msg(ctx.locale(), "welcome.set.message.ok")).queue();
    }

    private void setImage(CommandContext ctx) {
        String url = ctx.event().getOption("url").getAsString();
        if (!(url.startsWith("http://") || url.startsWith("https://"))) {
            ctx.hook().sendMessage(i18n.msg(ctx.locale(), "welcome.image.invalid")).queue();
            return;
        }
        GreetConfig cfg = GreetConfig.of(ctx.guild().getId());
        cfg.welcomeImageUrl = url;
        cfg.persistOrUpdate();
        ctx.hook().sendMessage(i18n.msg(ctx.locale(), "welcome.set.image.ok")).queue();
    }

    private void test(CommandContext ctx) {
        GreetConfig cfg = GreetConfig.findById(ctx.guild().getId());
        if (cfg == null || cfg.welcomeChannelId == null || cfg.welcomeMessage == null) {
            ctx.hook().sendMessage(i18n.msg(ctx.locale(), "welcome.not.configured")).queue();
            return;
        }
        var ch = ctx.guild().getChannelById(GuildMessageChannel.class, cfg.welcomeChannelId);
        if (ch == null) {
            ctx.hook().sendMessage(i18n.msg(ctx.locale(), "welcome.not.configured")).queue();
            return;
        }
        String msg = TemplateUtil.fill(cfg.welcomeMessage, ctx.member(), ctx.guild());

        var eb = new EmbedBuilder()
                .setColor(new Color(0x57F287))
                .setDescription(msg)
                .setThumbnail(ctx.member().getEffectiveAvatarUrl());
        if (cfg.welcomeImageUrl != null) eb.setImage(cfg.welcomeImageUrl);

        ch.sendMessageEmbeds(eb.build()).queue();
        ctx.hook().sendMessage(i18n.msg(ctx.locale(), "welcome.test.sent")).queue();
    }

    @Override public String name() { return "welcome"; }
}
