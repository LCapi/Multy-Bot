package multybot.core;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.InteractionHook;

import java.util.Locale;

/**
 * Per-interaction context for slash commands.
 * Provides safe access to JDA, guild, member, locale and reply helpers.
 */
public class CommandContext {

    private final SlashCommandInteractionEvent event;
    private final Locale locale;

    private CommandContext(SlashCommandInteractionEvent event, Locale locale) {
        this.event = event;
        this.locale = locale;
    }

    /** Main factory */
    public static CommandContext from(SlashCommandInteractionEvent event) {
        return new CommandContext(event, resolveLocale(event));
    }

    /** Alias for compatibility / readability */
    public static CommandContext fromEvent(SlashCommandInteractionEvent event) {
        return from(event);
    }

    /**
     * Resolve locale with the following precedence:
     * 1) User locale
     * 2) Guild locale
     * 3) JVM default locale
     */
    private static Locale resolveLocale(SlashCommandInteractionEvent e) {
        DiscordLocale user = e.getUserLocale();
        return Locale.forLanguageTag(user.getLocale());

    }

    // ------------- Context getters -------------

    public SlashCommandInteractionEvent event() {
        return event;
    }

    public Locale locale() {
        return locale;
    }

    public JDA jda() {
        return event.getJDA();
    }

    public Guild guild() {
        return event.getGuild();
    }

    public Member member() {
        return event.getMember();
    }

    // ------------- Reply helpers -------------

    /**
     * Safe reply helper.
     * If the interaction is not yet acknowledged, uses event.reply().
     * Otherwise, uses the interaction hook to send a follow-up message.
     */
    public void reply(String content) {
        if (event.isAcknowledged()) {
            hook().sendMessage(content).queue();
        } else {
            event.reply(content).queue();
        }
    }

    /**
     * Ephemeral reply helper.
     * If the interaction is not yet acknowledged, uses event.reply().setEphemeral(true).
     * Otherwise, uses the hook with an ephemeral follow-up.
     */
    public void replyEphemeral(String content) {
        if (event.isAcknowledged()) {
            hook().sendMessage(content)
                    .setEphemeral(true)
                    .queue();
        } else {
            event.reply(content)
                    .setEphemeral(true)
                    .queue();
        }
    }

    /**
     * Ensures the interaction is acknowledged (deferReply() if needed)
     * and returns the interaction hook.
     */
    public InteractionHook hook() {
        ensureAcknowledged();
        return event.getHook();
    }

    /** Convenience method to send an additional message after the first reply. */
    public void followup(String content) {
        hook().sendMessage(content).queue();
    }

    /**
     * If the interaction has not been acknowledged yet (no reply or defer),
     * it calls deferReply() to avoid the 3-second timeout.
     */
    public void ensureAcknowledged() {
        if (!event.isAcknowledged()) {
            event.deferReply().queue();
        }
    }
}