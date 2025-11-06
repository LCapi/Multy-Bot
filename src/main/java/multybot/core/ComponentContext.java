package multybot.core;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.InteractionHook;

import java.util.Locale;

public record ComponentContext(
        GenericComponentInteractionCreateEvent event,
        JDA jda,
        Guild guild,
        Member member,
        Locale locale
) {
    public static ComponentContext from(GenericComponentInteractionCreateEvent e) {
        Locale loc = resolveLocale(e);
        return new ComponentContext(e, e.getJDA(), e.getGuild(), e.getMember(), loc);
    }

    private static Locale resolveLocale(GenericComponentInteractionCreateEvent e) {
        DiscordLocale user = e.getUserLocale();
        if (user != null) return Locale.forLanguageTag(user.getLocale());
        Guild g = e.getGuild();
        if (g != null && g.getLocale() != null) {
            return Locale.forLanguageTag(g.getLocale().getLocale());
        }
        return Locale.getDefault();
    }

    public InteractionHook hook() {
        ensureAcknowledged();
        return event.getHook();
    }

    public void reply(String content) {
        event.reply(content).queue();
    }

    public void replyEphemeral(String content) {
        event.reply(content).setEphemeral(true).queue();
    }

    public void followup(String content) {
        hook().sendMessage(content).queue();
    }

    public void ensureAcknowledged() {
        if (!event.isAcknowledged()) {
            event.deferReply().queue();
        }
    }
}