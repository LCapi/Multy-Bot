package multybot.features.remind;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import multybot.core.*;
import multybot.infra.I18n;
import multybot.infra.TimeUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.bson.types.ObjectId;

import java.awt.*;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@ApplicationScoped
@DiscordCommand(name = "remind", descriptionKey = "remind.create.description")
@Cooldown(seconds = 3)
public class RemindCommand implements Command {

    @Inject I18n i18n;

    @Override
    public SlashCommandData slashData(Locale locale) {
        return Commands.slash("remind", i18n.msg(locale, "remind.create.description"))
                .addSubcommands(
                        new SubcommandData("create", i18n.msg(locale, "remind.create.description"))
                                .addOption(OptionType.STRING, "text", "Mensaje del recordatorio", true)
                                .addOption(OptionType.STRING, "in", "En (ej. 10m, 2h, 1d2h)", false)
                                .addOption(OptionType.STRING, "at", "Fecha/hora (ej. 2025-10-06 15:30)", false),
                        new SubcommandData("list", i18n.msg(locale, "remind.list.description")),
                        new SubcommandData("delete", i18n.msg(locale, "remind.delete.description"))
                                .addOption(OptionType.STRING, "id", "ID del recordatorio (ObjectId)", true)
                );
    }

    @Override
    public void execute(CommandContext ctx) throws Exception {
        var sub = ctx.event().getSubcommandName();
        switch (sub) {
            case "create" -> handleCreate(ctx);
            case "list"   -> handleList(ctx);
            case "delete" -> handleDelete(ctx);
            default -> ctx.hook().sendMessage("Unknown subcommand").queue();
        }
    }

    private void handleCreate(CommandContext ctx) {
        var ev = ctx.event();
        String text = ev.getOption("text").getAsString().trim();
        String inStr = ev.getOption("in") != null ? ev.getOption("in").getAsString() : null;
        String atStr = ev.getOption("at") != null ? ev.getOption("at").getAsString() : null;

        if ((inStr == null && atStr == null) || (inStr != null && atStr != null)) {
            ctx.hook().sendMessage(i18n.msg(ctx.locale(),"remind.create.invalid") + " " + i18n.msg(ctx.locale(),"remind.create.params")).queue();
            return;
        }

        Instant due;
        if (inStr != null) {
            Duration d = TimeUtil.parseDuration(inStr);
            if (d == null || d.isZero() || d.isNegative()) {
                ctx.hook().sendMessage(i18n.msg(ctx.locale(),"remind.create.parse.duration")).queue();
                return;
            }
            due = Instant.now().plus(d);
        } else {
            Instant at = TimeUtil.parseAt(atStr, TimeUtil.DEFAULT_ZONE);
            if (at == null || at.isBefore(Instant.now())) {
                ctx.hook().sendMessage(i18n.msg(ctx.locale(),"remind.create.parse.datetime")).queue();
                return;
            }
            due = at;
        }

        ReminderDoc r = new ReminderDoc();
        r.guildId = ctx.guild().getId();
        r.channelId = ctx.event().getChannel().getId();
        r.userId = ctx.member().getId();
        r.text = text;
        r.dueAt = Date.from(due);
        r.persist();

        String when = TimeUtil.fmtInstant(due, TimeUtil.DEFAULT_ZONE, ctx.locale());
        ctx.hook().sendMessage(i18n.msg(ctx.locale(),"remind.create.ok", when)).queue();
    }

    private void handleList(CommandContext ctx) {
        List<ReminderDoc> list = ReminderDoc.find("guildId = ?1 and userId = ?2 and status = ?3",
                        ctx.guild().getId(), ctx.member().getId(), "PENDING")
                .list();

        if (list.isEmpty()) {
            ctx.hook().sendMessage(i18n.msg(ctx.locale(), "remind.list.empty")).queue();
            return;
        }

        var eb = new EmbedBuilder()
                .setTitle(i18n.msg(ctx.locale(), "remind.list.title", list.size()))
                .setColor(new Color(0x57F287));

        for (ReminderDoc r : list) {
            String when = TimeUtil.fmtInstant(r.dueAt.toInstant(), TimeUtil.DEFAULT_ZONE, ctx.locale());
            eb.addField("ID: " + r.id.toHexString(), i18n.msg(ctx.locale(), "remind.item", r.id.toHexString(), when, r.text), false);
        }

        ctx.hook().sendMessageEmbeds(eb.build()).queue();
    }

    private void handleDelete(CommandContext ctx) {
        var ev = ctx.event();
        String id = ev.getOption("id").getAsString();
        if (!ObjectId.isValid(id)) {
            ctx.hook().sendMessage(i18n.msg(ctx.locale(), "remind.delete.notfound")).queue();
            return;
        }
        ReminderDoc r = ReminderDoc.findById(new ObjectId(id));
        if (r == null || !r.guildId.equals(ctx.guild().getId()) || !r.userId.equals(ctx.member().getId()) || !"PENDING".equals(r.status)) {
            ctx.hook().sendMessage(i18n.msg(ctx.locale(), "remind.delete.notfound")).queue();
            return;
        }
        r.delete();
        ctx.hook().sendMessage(i18n.msg(ctx.locale(), "remind.delete.ok")).queue();
    }

    @Override public String name() { return "remind"; }
}
