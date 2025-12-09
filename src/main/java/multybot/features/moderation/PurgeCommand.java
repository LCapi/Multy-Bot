package multybot.features.moderation;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import multybot.core.*;
import multybot.infra.I18n;
import multybot.infra.LogService;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@ApplicationScoped
@DiscordCommand(name = "purge", descriptionKey = "mod.purge.description")
@RequirePermissions({ Permission.MESSAGE_MANAGE })
@Cooldown(seconds = 5)
public class PurgeCommand implements Command {

    @Inject I18n i18n;
    @Inject LogService logs;
    @Inject ModerationCaseRegistry cases;

    @Override
    public SlashCommandData slashData(Locale locale) {
        return Commands.slash("purge", i18n.msg(locale, "mod.purge.description"))
                .addOption(OptionType.INTEGER, "amount", "Número de mensajes a borrar (1-100)", true)
                .addOption(OptionType.USER, "user", "Solo de este usuario", false);
    }

    @Override
    public void execute(CommandContext ctx) {
        var ch = ctx.event().getChannel();
        if (!(ch instanceof TextChannel tc)) {
            ctx.hook().sendMessage(i18n.msg(ctx.locale(), "mod.error.channel")).queue();
            return;
        }

        int amount = ctx.event().getOption("amount").getAsInt();
        if (amount < 1 || amount > 100) {
            ctx.hook().sendMessage(i18n.msg(ctx.locale(), "mod.error.purge.range")).queue();
            return;
        }

        var userOpt = ctx.event().getOption("user");
        var cutoff = OffsetDateTime.now().minusDays(14);

        // Load history and filter
        List<Message> messages = tc.getHistory().retrievePast(amount).complete();
        if (userOpt != null) {
            var uid = userOpt.getAsUser().getId();
            messages = messages.stream()
                    .filter(m -> m.getAuthor().getId().equals(uid))
                    .collect(Collectors.toList());
        }

        // Split by age
        List<Message> toBulk = messages.stream()
                .filter(m -> m.getTimeCreated().isAfter(cutoff))
                .collect(Collectors.toList());
        List<Message> toSingle = messages.stream()
                .filter(m -> m.getTimeCreated().isBefore(cutoff))
                .collect(Collectors.toList());

        if (!toBulk.isEmpty()) {
            tc.deleteMessages(toBulk).queue(); // may fail if any message is too old
        }
        for (Message m : toSingle) {
            m.delete().queue();
        }

        // Moderation case + log
        var mc = new ModerationCase();
        mc.guildId = ctx.guild().getId();
        mc.moderatorId = ctx.member().getId();
        mc.targetId = null;
        mc.type = ModerationType.PURGE;
        mc.reason = "amount=" + amount + (userOpt != null ? ", user=" + userOpt.getAsUser().getId() : "");
        cases.save(mc); // <--- sustituyendo mc.persist()

        logs.log(ctx.guild(), "**[PURGE]** %d mensajes en #%s (by <@%s>)".formatted(
                messages.size(), tc.getName(), ctx.member().getId()));

        if (!toSingle.isEmpty()) {
            ctx.hook().sendMessage(i18n.msg(ctx.locale(), "mod.error.purge.age")).queue();
        } else {
            ctx.hook().sendMessage(i18n.msg(ctx.locale(), "mod.done")).queue();
        }
    }

    @Override public String name() { return "purge"; }
}