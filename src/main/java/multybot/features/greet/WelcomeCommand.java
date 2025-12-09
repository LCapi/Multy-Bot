package multybot.features.greet;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import multybot.core.*;
import multybot.infra.I18n;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.awt.Color;
import java.util.Locale;
import java.util.Objects;

@ApplicationScoped
@DiscordCommand(name = "welcome", descriptionKey = "welcome.set.description")
@RequirePermissions({ Permission.MANAGE_SERVER })
@Cooldown()
public class WelcomeCommand extends AbstractCommand {

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
                                .addOption(OptionType.STRING, "url", "URL de imagen para la bienvenida", true),
                        new SubcommandData("test", i18n.msg(locale, "welcome.test.description"))
                );
    }

    @Override
    public void execute(CommandContext ctx) {
        String sub = ctx.event().getSubcommandName();
        if (sub == null) {
            ctx.replyEphemeral("Debes especificar un subcomando.");
            return;
        }
        switch (sub) {
            case "set-channel" -> setChannel(ctx);
            case "set-message" -> setMessage(ctx);
            case "set-image"   -> setImage(ctx);
            case "test"        -> test(ctx);
            default -> ctx.hook().sendMessage("Unknown subcommand").queue();
        }
    }

    private void setChannel(CommandContext ctx) {
        String channelId = Objects.requireNonNull(ctx.event().getOption("channel")).getAsChannel().getId(); // JDA 5: usamos id String
        GreetConfig cfg = GreetConfig.of(ctx.guild().getId());
        cfg.welcomeChannelId = channelId;
        GreetConfig.save(cfg);
        ctx.hook().sendMessage(i18n.msg(ctx.locale(), "welcome.set.channel.ok")).queue();
    }

    private void setMessage(CommandContext ctx) {
        String msg = Objects.requireNonNull(ctx.event().getOption("message")).getAsString();
        GreetConfig cfg = GreetConfig.of(ctx.guild().getId());
        cfg.welcomeMessage = msg;
        GreetConfig.save(cfg);
        ctx.hook().sendMessage(i18n.msg(ctx.locale(), "welcome.set.message.ok")).queue();
    }

    private void setImage(CommandContext ctx) {
        String url = Objects.requireNonNull(ctx.event().getOption("url")).getAsString();
        GreetConfig cfg = GreetConfig.of(ctx.guild().getId());
        cfg.welcomeImageUrl = url;
        GreetConfig.save(cfg);
        ctx.hook().sendMessage(i18n.msg(ctx.locale(), "welcome.set.image.ok")).queue();
    }

    private void test(CommandContext ctx) {
        GreetConfig cfg = GreetConfig.of(ctx.guild().getId());
        if (cfg.welcomeChannelId == null || cfg.welcomeChannelId.isBlank()) {
            ctx.replyEphemeral(i18n.msg(ctx.locale(), "welcome.test.nochannel"));
            return;
        }

        TextChannel ch = ctx.guild().getTextChannelById(cfg.welcomeChannelId);
        if (ch == null) {
            ctx.replyEphemeral(i18n.msg(ctx.locale(), "welcome.test.nochannel"));
            return;
        }

        String msg = TemplateUtil.fill(cfg.welcomeMessage, ctx.member(), ctx.guild());

        EmbedBuilder eb = new EmbedBuilder()
                .setColor(new Color(0x57F287))
                .setDescription(msg)
                .setThumbnail(ctx.member().getEffectiveAvatarUrl());

        if (cfg.welcomeImageUrl != null && !cfg.welcomeImageUrl.isBlank()) {
            eb.setImage(cfg.welcomeImageUrl);
        }

        ch.sendMessageEmbeds(eb.build()).queue();
        ctx.replyEphemeral(i18n.msg(ctx.locale(), "welcome.test.sent"));
    }

    @Override public String name() { return "welcome"; }
}
