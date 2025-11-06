package multybot.core;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.InteractionHook;

import java.util.Locale;

public class CommandContext {

    private final SlashCommandInteractionEvent event;
    private final Locale locale;

    private CommandContext(SlashCommandInteractionEvent event, Locale locale) {
        this.event = event;
        this.locale = locale;
    }

    /** Alias para compatibilidad con código antiguo */
    public static CommandContext fromEvent(SlashCommandInteractionEvent event) {
        return from(event);
    }

    /** Fábrica única */
    public static CommandContext from(SlashCommandInteractionEvent event) {
        return new CommandContext(event, resolveLocale(event));
    }

    private static Locale resolveLocale(SlashCommandInteractionEvent e) {
        // 1) Locale del usuario si existe
        DiscordLocale user = e.getUserLocale();
        if (user != null) return Locale.forLanguageTag(user.getLocale());

        // 2) Locale de la guild si existe
        Guild g = e.getGuild();
        if (g != null && g.getLocale() != null) {
            return Locale.forLanguageTag(g.getLocale().getLocale());
        }

        // 3) Fallback
        return Locale.getDefault();
    }

    /* ----------------- Getters de bajo nivel ----------------- */
    public SlashCommandInteractionEvent event() { return event; }
    public Locale locale() { return locale; }
    public JDA jda() { return event.getJDA(); }
    public Guild guild() { return event.getGuild(); }
    public Member member() { return event.getMember(); }

    /* ----------------- Helpers de respuesta ------------------ */

    /** Respuesta rápida normal */
    public void reply(String content) {
        event.reply(content).queue();
    }

    /** Respuesta efímera */
    public void replyEphemeral(String content) {
        event.reply(content).setEphemeral(true).queue();
    }

    /** Asegura que el interaction está reconocido y devuelve el hook */
    public InteractionHook hook() {
        ensureAcknowledged();
        return event.getHook();
    }

    /** Envío por hook (tras defer o primer reply) */
    public void followup(String content) {
        hook().sendMessage(content).queue();
    }

    /** Si aún no se reconoció, hacemos deferReply() para no caducar */
    public void ensureAcknowledged() {
        if (!event.isAcknowledged()) {
            event.deferReply().queue();
        }
    }
}