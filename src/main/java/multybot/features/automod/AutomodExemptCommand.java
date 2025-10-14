package multybot.features.automod;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import multybot.core.*;
import multybot.infra.I18n;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.*;

import java.awt.*;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

@ApplicationScoped
@DiscordCommand(name = "automod-exempt", descriptionKey = "automod.exempt.description")
@RequirePermissions({ Permission.MANAGE_SERVER }) // o MANAGE_GUILD según versión
@Cooldown(seconds = 5)
public class AutomodExemptCommand implements Command {

    @Inject I18n i18n;

    @Override
    public SlashCommandData slashData(Locale locale) {
        return Commands.slash("automod_exempt", i18n.msg(locale, "automod.exempt.description"))
                .addSubcommands(
                        new SubcommandData("add", i18n.msg(locale, "automod.exempt.add.description"))
                                .addOption(OptionType.STRING, "type", "role | channel | user", true)
                                .addOption(OptionType.ROLE, "role", "Rol a excluir", false)
                                .addOption(OptionType.CHANNEL, "channel", "Canal a excluir", false)
                                .addOption(OptionType.USER, "user", "Usuario a excluir", false),
                        new SubcommandData("remove", i18n.msg(locale, "automod.exempt.remove.description"))
                                .addOption(OptionType.STRING, "type", "role | channel | user", true)
                                .addOption(OptionType.ROLE, "role", "Rol a quitar de exención", false)
                                .addOption(OptionType.CHANNEL, "channel", "Canal a quitar", false)
                                .addOption(OptionType.USER, "user", "Usuario a quitar", false),
                        new SubcommandData("list", i18n.msg(locale, "automod.exempt.list.description"))
                );
    }

    @Override
    public void execute(CommandContext ctx) {
        var sub = ctx.event().getSubcommandName();
        switch (sub) {
            case "add"    -> handleAdd(ctx);
            case "remove" -> handleRemove(ctx);
            case "list"   -> handleList(ctx);
            default -> ctx.hook().sendMessage("Unknown subcommand").queue();
        }
    }

    private void handleAdd(CommandContext ctx) {
        String type = ctx.event().getOption("type").getAsString().toLowerCase();
        var cfg = AutomodConfig.loadOrDefault(ctx.guild().getId());

        switch (type) {
            case "role" -> {
                Role r = optRole(ctx);
                if (r == null) { ctx.hook().sendMessage(i18n.msg(ctx.locale(),"automod.exempt.error.input")).queue(); return; }
                cfg.exemptRoleIds.add(r.getId());
            }
            case "channel" -> {
                GuildChannel ch = optChannel(ctx);
                if (ch == null) { ctx.hook().sendMessage(i18n.msg(ctx.locale(),"automod.exempt.error.input")).queue(); return; }
                cfg.exemptChannelIds.add(ch.getId());
            }
            case "user" -> {
                User u = optUser(ctx);
                if (u == null) { ctx.hook().sendMessage(i18n.msg(ctx.locale(),"automod.exempt.error.input")).queue(); return; }
                cfg.exemptUserIds.add(u.getId());
            }
            default -> { ctx.hook().sendMessage(i18n.msg(ctx.locale(),"automod.exempt.error.kind")).queue(); return; }
        }
        cfg.persistOrUpdate();
        ctx.hook().sendMessage(i18n.msg(ctx.locale(),"automod.exempt.add.ok")).queue();
    }

    private void handleRemove(CommandContext ctx) {
        String type = ctx.event().getOption("type").getAsString().toLowerCase();
        var cfg = AutomodConfig.loadOrDefault(ctx.guild().getId());
        boolean removed = false;

        switch (type) {
            case "role" -> {
                Role r = optRole(ctx);
                if (r == null) { ctx.hook().sendMessage(i18n.msg(ctx.locale(),"automod.exempt.error.input")).queue(); return; }
                removed = cfg.exemptRoleIds.remove(r.getId());
            }
            case "channel" -> {
                GuildChannel ch = optChannel(ctx);
                if (ch == null) { ctx.hook().sendMessage(i18n.msg(ctx.locale(),"automod.exempt.error.input")).queue(); return; }
                removed = cfg.exemptChannelIds.remove(ch.getId());
            }
            case "user" -> {
                User u = optUser(ctx);
                if (u == null) { ctx.hook().sendMessage(i18n.msg(ctx.locale(),"automod.exempt.error.input")).queue(); return; }
                removed = cfg.exemptUserIds.remove(u.getId());
            }
            default -> { ctx.hook().sendMessage(i18n.msg(ctx.locale(),"automod.exempt.error.kind")).queue(); return; }
        }
        cfg.persistOrUpdate();
        ctx.hook().sendMessage(i18n.msg(ctx.locale(), removed ? "automod.exempt.remove.ok" : "automod.exempt.remove.notfound")).queue();
    }

    private void handleList(CommandContext ctx) {
        var cfg = AutomodConfig.loadOrDefault(ctx.guild().getId());

        if (cfg.exemptRoleIds.isEmpty() && cfg.exemptChannelIds.isEmpty() && cfg.exemptUserIds.isEmpty()) {
            ctx.hook().sendMessage(i18n.msg(ctx.locale(), "automod.exempt.list.empty")).queue();
            return;
        }

        String roles = cfg.exemptRoleIds.stream().map(id -> "<@&" + id + ">").collect(Collectors.joining(" "));
        String chans = cfg.exemptChannelIds.stream().map(id -> "<#" + id + ">").collect(Collectors.joining(" "));
        String users = cfg.exemptUserIds.stream().map(id -> "<@" + id + ">").collect(Collectors.joining(" "));

        var eb = new EmbedBuilder()
                .setTitle(i18n.msg(ctx.locale(), "automod.exempt.list.title"))
                .setColor(new Color(0x5865F2));
        if (!roles.isBlank()) eb.addField(i18n.msg(ctx.locale(), "automod.exempt.type.role"), roles, false);
        if (!chans.isBlank()) eb.addField(i18n.msg(ctx.locale(), "automod.exempt.type.channel"), chans, false);
        if (!users.isBlank()) eb.addField(i18n.msg(ctx.locale(), "automod.exempt.type.user"), users, false);

        ctx.hook().sendMessageEmbeds(eb.build()).queue();
    }

    private Role optRole(CommandContext ctx) {
        var o = ctx.event().getOption("role"); return o == null ? null : o.getAsRole();
    }
    private GuildChannel optChannel(CommandContext ctx) {
        // en AutomodExemptCommand.handleAdd/remove para type "channel":
        String channelId = ctx.event().getOption("channel").getAsChannel().getId();
        cfg.exemptChannelIds.add(channelId); // o .remove(channelId)
    }
    private User optUser(CommandContext ctx) {
        var o = ctx.event().getOption("user"); return o == null ? null : o.getAsUser();
    }

    @Override public String name() { return "automod_exempt"; }
}
