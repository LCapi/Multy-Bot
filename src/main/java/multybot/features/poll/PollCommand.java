package multybot.features.poll;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import multybot.core.*;
import multybot.infra.I18n;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.*;

import org.bson.types.ObjectId;

import java.awt.*;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@ApplicationScoped
@DiscordCommand(name="poll", descriptionKey="poll.create.description")
@Cooldown(seconds = 5)
public class PollCommand implements Command {

    @Inject I18n i18n;

    @Override
    public SlashCommandData slashData(Locale locale) {
        return Commands.slash("poll", i18n.msg(locale, "poll.create.description"))
                .addSubcommands(
                        new SubcommandData("create", i18n.msg(locale, "poll.create.description"))
                                .addOption(OptionType.STRING, "question", "Pregunta", true)
                                .addOption(OptionType.STRING, "options", "Opciones separadas por ';' (2-10)", true),
                        new SubcommandData("close", i18n.msg(locale, "poll.close.description"))
                                .addOption(OptionType.STRING, "id", "ID de la encuesta (ObjectId)", true)
                );
    }

    @Override
    public void execute(CommandContext ctx) throws Exception {
        var ev = ctx.event();
        String sub = ev.getSubcommandName();
        if ("create".equals(sub)) createPoll(ctx);
        else if ("close".equals(sub)) closePoll(ctx);
        else ctx.hook().sendMessage("Unknown subcommand").queue();
    }

    private void createPoll(CommandContext ctx) {
        var ev = ctx.event();
        String q = ev.getOption("question").getAsString().trim();
        String raw = ev.getOption("options").getAsString();
        String[] parts = raw.split(";");
        List<String> opts = new ArrayList<>();
        for (String p : parts) {
            String s = p.trim();
            if (!s.isEmpty()) opts.add(s);
        }
        if (opts.size() < 2 || opts.size() > 10) {
            ctx.hook().sendMessage(i18n.msg(ctx.locale(), "poll.invalid.options")).queue();
            return;
        }

        // persistimos antes para conocer el id y usarlo en customId de botones
        PollDoc poll = new PollDoc();
        poll.guildId = ctx.guild().getId();
        poll.channelId = ctx.event().getChannel().getId();
        poll.question = q;
        poll.options = opts;
        poll.persist();

        // build embed + buttons
        var eb = new EmbedBuilder()
                .setTitle("🗳️ " + q)
                .setColor(new Color(0x5865F2));
        for (int i = 0; i < opts.size(); i++) {
            eb.addField((i+1)+".", opts.get(i), true);
        }

        List<Button> buttons = new ArrayList<>();
        for (int i = 0; i < opts.size(); i++) {
            String cid = "poll:vote:" + poll.id.toHexString() + ":" + i;
            buttons.add(Button.primary(cid, Integer.toString(i+1)));
        }
        List<ActionRow> rows = new ArrayList<>();
        for (int i = 0; i < buttons.size(); i += 5) {
            rows.add(ActionRow.of(buttons.subList(i, Math.min(i+5, buttons.size()))));
        }

        var hook = ctx.hook();
        hook.sendMessageEmbeds(eb.build()).setComponents(rows).queue(msg -> {
            poll.messageId = msg.getId();
            poll.update();
            hook.sendMessage(MessageFormat.format(i18n.msg(ctx.locale(),"poll.created"), poll.id.toHexString()))
                    .setEphemeral(true).queue();
        });
    }

    private void closePoll(CommandContext ctx) {
        String id = ctx.event().getOption("id").getAsString();
        if (!ObjectId.isValid(id)) {
            ctx.hook().sendMessage(i18n.msg(ctx.locale(), "poll.notfound")).queue();
            return;
        }
        PollDoc poll = PollDoc.findById(new ObjectId(id));
        if (poll == null || !poll.guildId.equals(ctx.guild().getId())) {
            ctx.hook().sendMessage(i18n.msg(ctx.locale(), "poll.notfound")).queue();
            return;
        }
        if (poll.closed) {
            ctx.hook().sendMessage(i18n.msg(ctx.locale(), "poll.already.closed")).queue();
            return;
        }

        poll.closed = true;
        poll.closedAt = new java.util.Date();
        poll.update();

        // deshabilitar botones en el mensaje original
        var ch = ctx.jda().getTextChannelById(poll.channelId);
        if (ch != null) {
            var msg = ch.retrieveMessageById(poll.messageId).complete();
            if (msg != null) {
                // reconstruimos los mismos botones pero disabled
                List<Button> buttons = new ArrayList<>();
                for (int i = 0; i < poll.options.size(); i++) {
                    String cid = "poll:vote:" + poll.id.toHexString() + ":" + i;
                    buttons.add(Button.secondary(cid, Integer.toString(i+1)).asDisabled());
                }
                List<ActionRow> rows = new ArrayList<>();
                for (int i = 0; i < buttons.size(); i += 5) {
                    rows.add(ActionRow.of(buttons.subList(i, Math.min(i+5, buttons.size()))));
                }
                msg.editMessageComponents(rows).queue();
            }
        }

        // publicar resultados en el canal
        var res = PollUtil.resultsEmbed(ctx, poll);
        if (ch != null) ch.sendMessageEmbeds(res.build()).queue();

        ctx.hook().sendMessage(i18n.msg(ctx.locale(), "poll.closed")).queue();
    }

    @Override public String name() { return "poll"; }
}
