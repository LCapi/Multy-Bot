package multybot.features.poll;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import multybot.core.ComponentContext;
import multybot.core.ComponentHandler;
import multybot.infra.I18n;

@ApplicationScoped
public class PollVoteHandler implements ComponentHandler {

    @Inject I18n i18n;
    @Inject PollRegistry pollRegistry;

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
        try {
            optionIdx = Integer.parseInt(parts[3]);
        } catch (NumberFormatException e) {
            return;
        }

        // Buscar poll en el registry (sin Mongo)
        PollDoc poll = pollRegistry.findById(pollId).orElse(null);
        if (poll == null) return;
        if (!poll.guildId.equals(ctx.guild().getId())) return;

        // Sanity check por si alguien manipula el customId
        if (optionIdx < 0 || optionIdx >= poll.options.size()) {
            ev.reply("Invalid option").setEphemeral(true).queue();
            return;
        }

        if (poll.closed) {
            ev.reply(i18n.msg(ctx.locale(), "poll.vote.closed"))
                    .setEphemeral(true)
                    .queue();
            return;
        }

        String userId = ctx.member().getId();
        Integer prev = poll.votes.get(userId);

        poll.votes.put(userId, optionIdx);
        pollRegistry.save(poll); // en vez de poll.update()

        String label = poll.options.get(optionIdx);
        if (prev == null || prev == optionIdx) {
            ev.reply(i18n.msg(ctx.locale(), "poll.vote.ok", label))
                    .setEphemeral(true)
                    .queue();
        } else {
            ev.reply(i18n.msg(ctx.locale(), "poll.vote.changed", label))
                    .setEphemeral(true)
                    .queue();
        }
    }
}