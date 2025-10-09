package multybot.features.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import multybot.core.*;
import multybot.infra.I18n;
import multybot.infra.persistence.GuildConfig;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.*;

import java.awt.*;
import java.util.Locale;
import java.util.Objects;

@ApplicationScoped
@DiscordCommand(name = "config", descriptionKey = "config.description")
@RequirePermissions({ Permission.MANAGE_SERVER }) // o MANAGE_GUILD según tu JDA
@Cooldown(seconds = 3)
public class ConfigCommand implements Command {

    @Inject I18n i18n;

    @Override
    public SlashCommandData slashData(Locale locale) {
        // /config set log-channel ... | /config set locale ...
        // /config show
        return Commands.slash("config", i18n.msg(locale, "config.description"))
                .addSubcommandGroups(
                        new SubcommandGroupData("set", i18n.msg(locale, "config.set.description"))
                                .addSubcommands(
                                        new SubcommandData("log-channel", i18n.msg(locale, "config.set.log.description"))
                                                .addOption(OptionType.CHANNEL, "channel", "Canal para logs", true),
                                        new SubcommandData("locale", i18n.msg(locale, "config.set.locale.description"))
                                                .addOption(OptionType.STRING, "value", "es o en", true)
                                )
                )
                .addSubcommands(
                        new SubcommandData("show", i18n.msg(locale, "config.show.description"))
                );
    }

    @Override
    public void execute(CommandContext ctx) {
        var ev = ctx.event();
        String group = ev.getSubcommandGroup();
        String sub   = ev.getSubcommandName();

        if (Objects.equals(sub, "show") && group == null) {
            handleShow(ctx);
            return;
        }
        if (Objects.equals(group, "set")) {
            switch (sub) {
                case "log-channel" -> handleSetLogChannel(ctx);
                case "locale"      -> handleSetLocale(ctx);
                default -> ctx.hook().sendMessage("Unknown subcommand").queue();
            }
            return;
        }
        ctx.hook().sendMessage("Unknown subcommand").queue();
    }

    private void handleSetLogChannel(CommandContext ctx) {
        var opt = ctx.event().getOption("channel");
        var asAny = opt.getAsChannel();
        if (!(asAny instanceof GuildMessageChannel ch)) {
            ctx.hook().sendMessage("❌ El canal debe permitir mensajes.").queue();
            return;
        }
        var cfg = GuildConfig.of(ctx.guild().getId());
        cfg.logChannelId = ch.getId();
        cfg.persistOrUpdate();

        ctx.hook().sendMessage(i18n.msg(ctx.locale(), "config.set.log.ok")).queue();
    }

    private void handleSetLocale(CommandContext ctx) {
        String v = ctx.event().getOption("value").getAsString().trim().toLowerCase(Locale.ROOT);
        if (!v.equals("es") && !v.equals("en")) {
            ctx.hook().sendMessage(i18n.msg(ctx.locale(), "config.set.locale.invalid")).queue();
            return;
        }
        var cfg = GuildConfig.of(ctx.guild().getId());
        cfg.locale = v;
        cfg.persistOrUpdate();

        // Responder ya en el nuevo idioma
        Locale newLoc = new Locale(v);
        String display = v.equals("es") ? "Español" : "English";
        ctx.hook().sendMessage(i18n.msg(newLoc, "config.set.locale.ok", display)).queue();
    }

    private void handleShow(CommandContext ctx) {
        var cfg = GuildConfig.of(ctx.guild().getId());
        String locale = cfg.locale == null ? "es" : cfg.locale;
        String logCh = cfg.logChannelId == null ? i18n.msg(ctx.locale(), "config.notset")
                : "<#" + cfg.logChannelId + ">";

        var eb = new EmbedBuilder()
                .setTitle(i18n.msg(ctx.locale(), "config.show.title"))
                .setColor(new Color(0x5865F2))
                .addField(i18n.msg(ctx.locale(), "config.show.locale"), locale, true)
                .addField(i18n.msg(ctx.locale(), "config.show.log"),    logCh, true);

        ctx.hook().sendMessageEmbeds(eb.build()).queue();
    }

    @Override public String name() { return "config"; }
}
