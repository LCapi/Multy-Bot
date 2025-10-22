package multybot.features.poll;

import jakarta.enterprise.context.ApplicationScoped;
import multybot.core.ComponentContext;
import multybot.core.ComponentHandler;
import multybot.infra.I18n;
import jakarta.inject.Inject;
import org.bson.types.ObjectId;

@ApplicationScoped
public class PollVoteHandler implements ComponentHandler {

    @Inject I18n i18n;

    @Override
    public boolean matches(String customId) {
        return customId != null && customId.startsWith("poll:vote:");
    }

    @Override
    public void handle(ComponentContext ctx) {
        var ev = ctx.event();
        String[] parts = ev.getComponentId().split(":");
        if (parts.length != 4) return;

        String pollId = parts[2];
        int optionIdx;
        try { optionIdx = Integer.parseInt(parts[3]); } catch (NumberFormatException e) { return; }

        if (!ObjectId.isValid(pollId)) return;
        PollDoc poll = PollDoc.findById(new ObjectId(pollId));
        if (poll == null || !poll.guildId.equals(ctx.guild().getId())) return;

        if (poll.closed) {
            ev.reply(i18n.msg(ctx.locale(), "poll.vote.closed")).setEphemeral(true).queue();
            return;
        }

        String userId = ctx.member().getId();
        Integer prev = poll.votes.get(userId);

        poll.votes.put(userId, optionIdx);
        poll.update();

        String label = poll.options.get(optionIdx);
        if (prev == null) {
            ev.reply(i18n.msg(ctx.locale(), "poll.vote.ok", label)).setEphemeral(true).queue();
        } else if (prev == optionIdx) {
            ev.reply(i18n.msg(ctx.locale(), "poll.vote.ok", label)).setEphemeral(true).queue();
        } else {
            ev.reply(i18n.msg(ctx.locale(), "poll.vote.changed", label)).setEphemeral(true).queue();
        }
    }
}
