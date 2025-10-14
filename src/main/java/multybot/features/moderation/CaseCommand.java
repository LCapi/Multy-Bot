package multybot.features.moderation;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import multybot.core.*;
import multybot.infra.I18n;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import io.quarkus.mongodb.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import org.bson.types.ObjectId;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
@DiscordCommand(name = "case", descriptionKey = "case.list.description")
@RequirePermissions({ Permission.KICK_MEMBERS })
@Cooldown(seconds = 3)
public class CaseCommand implements Command {

    @Inject I18n i18n;

    private static final int PAGE_SIZE = 10;

    @Override
    public SlashCommandData slashData(Locale locale) {
        // Construimos /case con subcomandos list|show|delete
        var data = Commands.slash("case", i18n.msg(locale, "case.list.description"))
                .addSubcommands(
                        new SubcommandData("list", i18n.msg(locale, "case.list.description"))
                                .addOption(OptionType.INTEGER, "page", "Número de página (>=1)", false)
                                .addOption(OptionType.USER, "user", "Filtrar por usuario objetivo", false)
                                .addOption(OptionType.STRING, "type", "Tipo de caso", false,
                                        Arrays.stream(i18n.msg(locale, "case.type").split(","))
                                                .map(t -> new net.dv8tion.jda.api.interactions.commands.build.Command.Choice(t, t))
                                                .toList()
                                ),
                        new SubcommandData("show", i18n.msg(locale, "case.show.description"))
                                .addOption(OptionType.STRING, "id", "ID del caso (ObjectId)", true),
                        new SubcommandData("delete", i18n.msg(locale, "case.delete.description"))
                                .addOption(OptionType.STRING, "id", "ID del caso (ObjectId)", true)
                );

        return data;
    }

    @Override
    public void execute(CommandContext ctx) {
        var ev = ctx.event();
        var sub = ev.getSubcommandName();
        if (sub == null) sub = "list";

        switch (sub) {
            case "list" -> handleList(ctx);
            case "show" -> handleShow(ctx);
            case "delete" -> handleDelete(ctx);
            default -> ctx.hook().sendMessage("Unknown subcommand").queue();
        }
    }

    private void handleList(CommandContext ctx) {
        var ev = ctx.event();
        int page = ev.getOption("page") != null ? Math.max(1, ev.getOption("page").getAsInt()) : 1;
        String userId = null;
        if (ev.getOption("user") != null) {
            User u = ev.getOption("user").getAsUser();
            if (u != null) userId = u.getId();
        }
        String typeStr = ev.getOption("type") != null ? ev.getOption("type").getAsString() : null;
        ModerationType type = null;
        if (typeStr != null) {
            try { type = ModerationType.valueOf(typeStr); } catch (IllegalArgumentException ignore) {}
        }

        // Construir query dinámico
        StringBuilder q = new StringBuilder("guildId = ?1");
        List<Object> params = new ArrayList<>();
        params.add(ctx.guild().getId());
        int idx = 2;
        if (userId != null) { q.append(" and targetId = ?").append(idx++); params.add(userId); }
        if (type != null)  { q.append(" and type = ?").append(idx); params.add(type); }

        PanacheQuery<ModerationCase> query =
                ModerationCase.find("guildId", gid).page(page).list();

        long total = query.count();
        int pageCount = (int) Math.max(1, Math.ceil(total / (double) PAGE_SIZE));
        int pageIndex = Math.min(page - 1, pageCount - 1);

        query.page(Page.of(pageIndex, PAGE_SIZE));
        List<ModerationCase> list = query.list();

        if (list.isEmpty()) {
            ctx.hook().sendMessage(i18n.msg(ctx.locale(), "case.list.empty")).queue();
            return;
        }

        String filters = (userId != null ? "user=<@" + userId + ">" : "user=any")
                + ", type=" + (type != null ? type : "any");

        var eb = new EmbedBuilder()
                .setColor(new Color(0x5865F2))
                .setTitle(MessageFormat.format(i18n.msg(ctx.locale(), "case.list.title"), page, pageCount))
                .setFooter(MessageFormat.format(i18n.msg(ctx.locale(), "case.footer"), filters));

        int n = pageIndex * PAGE_SIZE;
        for (ModerationCase mc : list) {
            ++n;
            String line = MessageFormat.format(
                    i18n.msg(ctx.locale(), "case.item"),
                    mc.id.toHexString(),
                    mc.type,
                    mc.targetId == null ? "—" : mc.targetId,
                    mc.createdAt
            );
            String reason = mc.reason == null ? "" : " · " + mc.reason;
            eb.addField("#" + n, line + reason, false);
        }

        ctx.hook().sendMessageEmbeds(eb.build()).queue();
    }

    private void handleShow(CommandContext ctx) {
        var ev = ctx.event();
        String id = ev.getOption("id").getAsString();
        if (!ObjectId.isValid(id)) {
            ctx.hook().sendMessage(i18n.msg(ctx.locale(), "case.error.id")).queue();
            return;
        }
        var mc = ModerationCase.findById(new ObjectId(id));
        if (mc == null || !Objects.equals(mc.guildId, ctx.guild().getId())) {
            ctx.hook().sendMessage(i18n.msg(ctx.locale(), "case.show.notfound")).queue();
            return;
        }

        var eb = new EmbedBuilder()
                .setColor(new Color(0xFEE75C))
                .setTitle("Case " + mc.id.toHexString())
                .addField("Tipo", String.valueOf(mc.type), true)
                .addField("Usuario", mc.targetId == null ? "—" : "<@" + mc.targetId + ">", true)
                .addField("Moderador", mc.moderatorId == null ? "—" : "<@" + mc.moderatorId + ">", true)
                .addField("Creado", mc.createdAt.toString(), true);
        if (mc.expiresAt != null) eb.addField("Expira", mc.expiresAt.toString(), true);
        eb.addField("Razón", mc.reason == null ? "—" : mc.reason, false);

        ctx.hook().sendMessageEmbeds(eb.build()).queue();
    }

    private void handleDelete(CommandContext ctx) {
        var ev = ctx.event();
        String id = ev.getOption("id").getAsString();
        if (!ObjectId.isValid(id)) {
            ctx.hook().sendMessage(i18n.msg(ctx.locale(), "case.error.id")).queue();
            return;
        }
        boolean ok = ModerationCase.deleteById(new ObjectId(id));
        ctx.hook().sendMessage(i18n.msg(ctx.locale(), ok ? "case.delete.ok" : "case.delete.notfound")).queue();
    }

    @Override public String name() { return "case"; }
}
