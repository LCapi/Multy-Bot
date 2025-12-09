package multybot.features.moderation;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import multybot.core.*;
import multybot.infra.I18n;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.awt.Color;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

@ApplicationScoped
@DiscordCommand(name = "case", descriptionKey = "case.description")
@RequirePermissions({ Permission.MANAGE_SERVER })
@Cooldown(seconds = 3)
public class CaseCommand implements Command {

    @Inject I18n i18n;
    @Inject ModerationCaseRegistry cases;

    private static final int PAGE_SIZE = 10;

    @Override
    public SlashCommandData slashData(Locale locale) {
        return Commands.slash("case", i18n.msg(locale, "case.description"))
                .addSubcommands(
                        new SubcommandData("list", i18n.msg(locale, "case.list.description"))
                                .addOption(OptionType.INTEGER, "page", i18n.msg(locale, "case.list.page"), false)
                                .addOption(OptionType.USER,    "user", i18n.msg(locale, "case.list.user"), false)
                                .addOption(OptionType.STRING,  "type", i18n.msg(locale, "case.list.type"), false),
                        new SubcommandData("show", i18n.msg(locale, "case.show.description"))
                                .addOption(OptionType.STRING, "id", "Case ID", true),
                        new SubcommandData("delete", i18n.msg(locale, "case.delete.description"))
                                .addOption(OptionType.STRING, "id", "Case ID", true),
                        new SubcommandData("reason", i18n.msg(locale, "case.reason.description"))
                                .addOption(OptionType.STRING, "id",     "Case ID", true)
                                .addOption(OptionType.STRING, "reason", "New reason", true)
                );
    }

    @Override
    public void execute(CommandContext ctx) {
        String sub = ctx.event().getSubcommandName();
        try {
            switch (sub) {
                case "list"   -> handleList(ctx);
                case "show"   -> handleShow(ctx);
                case "delete" -> handleDelete(ctx);
                case "reason" -> handleReason(ctx);
                default -> ctx.hook().sendMessage("Unknown subcommand").queue();
            }
        } catch (Exception e) {
            ctx.hook().sendMessage("X " + e.getMessage()).queue();
        }
    }

    private void handleList(CommandContext ctx) {
        String gid = ctx.guild().getId();
        Integer pageOpt = ctx.event().getOption("page") != null
                ? ctx.event().getOption("page").getAsInt()
                : 1;
        int page = (pageOpt == null || pageOpt < 1) ? 1 : pageOpt;

        String userId = ctx.event().getOption("user") != null
                ? ctx.event().getOption("user").getAsUser().getId()
                : null;
        String typeStr = ctx.event().getOption("type") != null
                ? ctx.event().getOption("type").getAsString()
                : null;

        ModerationType typeFilter = null;
        if (typeStr != null) {
            try {
                typeFilter = ModerationType.valueOf(typeStr.toUpperCase(Locale.ROOT));
            } catch (Exception ignored) {}
        }

        // Obtener todos los casos del guild desde la registry
        List<ModerationCase> all = cases.findByGuild(gid);

        // Filtros en memoria
        if (userId != null) {
            all = all.stream()
                    .filter(mc -> userId.equals(mc.targetId))
                    .collect(Collectors.toList());
        }
        if (typeFilter != null) {
            ModerationType tf = typeFilter;
            all = all.stream()
                    .filter(mc -> mc.type == tf)
                    .collect(Collectors.toList());
        }

        // Orden por fecha desc
        all.sort(Comparator.comparing((ModerationCase mc) -> mc.createdAt).reversed());

        int from = (page - 1) * PAGE_SIZE;
        int to = Math.min(from + PAGE_SIZE, all.size());
        if (from >= all.size()) {
            ctx.hook().sendMessage(i18n.msg(ctx.locale(), "case.list.empty")).queue();
            return;
        }
        List<ModerationCase> slice = all.subList(from, to);

        EmbedBuilder eb = new EmbedBuilder()
                .setTitle(i18n.msg(ctx.locale(), "case.list.title") + " • p." + page)
                .setColor(new Color(0x5865F2));

        StringBuilder sb = new StringBuilder();
        for (ModerationCase mc : slice) {
            String id = (mc.id != null ? mc.id : "?");
            String shortId = id.length() > 6 ? id.substring(0, 6) : id;

            String line = "`#" + shortId + "` **" + mc.type + "** " +
                    "<@" + mc.targetId + "> · " +
                    i18n.msg(ctx.locale(), "case.by") + " <@" + mc.moderatorId + ">" +
                    (mc.reason != null && !mc.reason.isBlank() ? " — " + mc.reason : "");
            sb.append(line).append("\n");
        }
        eb.setDescription(sb.toString());
        ctx.hook().sendMessageEmbeds(eb.build()).queue();
    }

    private void handleShow(CommandContext ctx) {
        String idStr = ctx.event().getOption("id").getAsString().trim();

        ModerationCase mc = cases.findById(idStr).orElse(null);
        if (mc == null || !Objects.equals(mc.guildId, ctx.guild().getId())) {
            ctx.hook().sendMessage(i18n.msg(ctx.locale(), "case.show.notfound")).queue();
            return;
        }

        EmbedBuilder eb = new EmbedBuilder()
                .setTitle(i18n.msg(ctx.locale(), "case.show.title") + " #" + mc.id)
                .setColor(new Color(0x57F287))
                .addField("Type", String.valueOf(mc.type), true)
                .addField("Target", "<@" + mc.targetId + ">", true)
                .addField("Moderator", "<@" + mc.moderatorId + ">", true)
                .addField("Created", mc.createdAt != null ? mc.createdAt.toString() : "-", true)
                .addField("Expires", mc.expiresAt != null ? mc.expiresAt.toString() : "-", true)
                .addField("Reason", mc.reason != null ? mc.reason : "-", false);

        ctx.hook().sendMessageEmbeds(eb.build()).queue();
    }

    private void handleDelete(CommandContext ctx) {
        String idStr = ctx.event().getOption("id").getAsString().trim();

        boolean ok = cases.delete(idStr);
        ctx.hook().sendMessage(
                ok
                        ? i18n.msg(ctx.locale(), "case.delete.ok")
                        : i18n.msg(ctx.locale(), "case.delete.notfound")
        ).queue();
    }

    private void handleReason(CommandContext ctx) {
        String idStr = ctx.event().getOption("id").getAsString().trim();
        String reason = ctx.event().getOption("reason").getAsString();

        ModerationCase mc = cases.findById(idStr).orElse(null);
        if (mc == null || !Objects.equals(mc.guildId, ctx.guild().getId())) {
            ctx.hook().sendMessage(i18n.msg(ctx.locale(), "case.show.notfound")).queue();
            return;
        }

        mc.reason = reason;
        cases.save(mc);

        ctx.hook().sendMessage(i18n.msg(ctx.locale(), "case.reason.ok")).queue();
    }

    @Override
    public String name() {
        return "case";
    }
}